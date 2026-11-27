# ---------------------------------------------------------------------------
# Infraestructura del Gestor de Proyectos en AWS
#   - VPC por defecto de la cuenta
#   - Grupo de seguridad minimo: SSH solo desde la IP del administrador y HTTP publico
#   - Llave SSH propia (se registra solo la llave publica)
#   - Instancia EC2 Ubuntu 24.04 con Docker, Compose y memoria swap (cloud-init)
#   - IP elastica para que la direccion publica no cambie al reiniciar la instancia
# ---------------------------------------------------------------------------

data "aws_vpc" "default" {
  default = true
}

# Imagen oficial de Ubuntu 24.04 LTS publicada por Canonical
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_key_pair" "gestor" {
  key_name   = "gestor-proyectos-llave"
  public_key = file(pathexpand(var.ruta_llave_publica))
}

resource "aws_security_group" "gestor" {
  name        = "gestor-proyectos-sg"
  description = "Gestor de Proyectos: SSH restringido y HTTP publico"
  vpc_id      = data.aws_vpc.default.id

  tags = {
    Name = "gestor-proyectos-sg"
  }
}

resource "aws_vpc_security_group_ingress_rule" "ssh_admin" {
  security_group_id = aws_security_group.gestor.id
  description       = "SSH solo desde la IP del administrador"
  cidr_ipv4         = var.mi_ip_cidr
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
}

resource "aws_vpc_security_group_ingress_rule" "http_publico" {
  security_group_id = aws_security_group.gestor.id
  description       = "Cliente web del Gestor de Proyectos"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
}

resource "aws_vpc_security_group_egress_rule" "salida" {
  security_group_id = aws_security_group.gestor.id
  description       = "Salida a internet (paquetes, imagenes Docker y tunel de Cloudflare)"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_instance" "gestor" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.tipo_instancia
  key_name               = aws_key_pair.gestor.key_name
  vpc_security_group_ids = [aws_security_group.gestor.id]

  root_block_device {
    volume_size           = var.disco_gb
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true
  }

  # Solo IMDSv2 (tokens obligatorios) para proteger los metadatos de la instancia
  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    repo_url = var.repo_url
    rama     = var.rama
  })

  # Una AMI nueva de Ubuntu o un cambio en el script de arranque no deben recrear la instancia
  lifecycle {
    ignore_changes = [ami, user_data]
  }

  tags = {
    Name = "gestor-proyectos-ec2"
  }
}

resource "aws_eip" "gestor" {
  domain   = "vpc"
  instance = aws_instance.gestor.id

  tags = {
    Name = "gestor-proyectos-ip"
  }
}
