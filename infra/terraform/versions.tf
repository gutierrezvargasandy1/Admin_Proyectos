terraform {
  required_version = ">= 1.9.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.region

  # Etiquetas que se aplican a todos los recursos creados por Terraform
  default_tags {
    tags = {
      Proyecto    = "GestorDeProyectos"
      Gestionado  = "Terraform"
      Responsable = var.responsable
      Unidad      = "U3-DevOps"
    }
  }
}
