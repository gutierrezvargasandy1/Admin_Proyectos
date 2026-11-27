variable "region" {
  description = "Region de AWS donde se crea la infraestructura"
  type        = string
  default     = "us-east-1"
}

variable "tipo_instancia" {
  description = "Tipo de instancia EC2 (t3.small: 2 vCPU y 2 GB de RAM, incluido en el plan gratuito)"
  type        = string
  default     = "t3.small"
}

variable "disco_gb" {
  description = "Tamano del disco raiz en GB"
  type        = number
  default     = 20
}

variable "mi_ip_cidr" {
  description = "IP publica del administrador en formato CIDR (ej. 201.10.20.30/32). Solo esta IP puede entrar por SSH"
  type        = string

  validation {
    condition     = can(cidrhost(var.mi_ip_cidr, 0)) && endswith(var.mi_ip_cidr, "/32")
    error_message = "mi_ip_cidr debe ser una sola IP con mascara /32, por ejemplo 201.10.20.30/32."
  }
}

variable "ruta_llave_publica" {
  description = "Ruta de la llave publica SSH que se registra en AWS (la privada nunca sale del equipo)"
  type        = string
  default     = "~/.ssh/gestor_aws.pub"
}

variable "repo_url" {
  description = "Repositorio que se clona en la maquina virtual"
  type        = string
  default     = "https://github.com/gutierrezvargasandy1/Admin_Proyectos.git"
}

variable "rama" {
  description = "Rama del repositorio que se despliega"
  type        = string
  default     = "main"
}

variable "responsable" {
  description = "Nombre del responsable (etiqueta de los recursos)"
  type        = string
  default     = "Jose Andres Gutierrez Vargas"
}
