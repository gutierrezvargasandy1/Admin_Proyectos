output "ip_publica" {
  description = "IP elastica de la instancia"
  value       = aws_eip.gestor.public_ip
}

output "id_instancia" {
  description = "Identificador de la instancia EC2"
  value       = aws_instance.gestor.id
}

output "ami_utilizada" {
  description = "AMI de Ubuntu 24.04 con la que se creo la instancia"
  value       = aws_instance.gestor.ami
}

output "url_aplicacion" {
  description = "Cliente web publicado por HTTP"
  value       = "http://${aws_eip.gestor.public_ip}"
}

output "comando_ssh" {
  description = "Conexion SSH a la instancia"
  value       = "ssh -i ~/.ssh/gestor_aws ubuntu@${aws_eip.gestor.public_ip}"
}

output "comando_tunel_monitoreo" {
  description = "Tunel SSH para abrir Grafana (localhost:3000) y Prometheus (localhost:9090)"
  value       = "ssh -i ~/.ssh/gestor_aws -N -L 3000:127.0.0.1:3000 -L 9090:127.0.0.1:9090 ubuntu@${aws_eip.gestor.public_ip}"
}
