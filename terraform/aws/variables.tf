variable "environment" {
  description = "Environment name (development, production, etc)"
  type        = string
}

variable "region" {
  description = "AWS region to target"
  type        = string
}

variable "enable_network" {
  description = "Use network module. Set to false to use your own network resources"
  type        = bool
  default     = true
}

variable "vpc_id" {
  description = "AWS VPC ID (if not using network module)"
  type        = string
  default     = ""
}

variable "vpc_cidr" {
  description = "RFC1918 CIDR range for VPC"
  type        = string
  default     = ""
}

variable "public_cidr" {
  description = "RFC1918 CIDR range for public subnets (subset of vpc_cidr)"
  type        = string
  default     = ""
}

variable "private_cidr" {
  description = "RFC1918 CIDR range for private subnets (subset of vpc_cidr)"
  type        = string
  default     = ""
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs for deployment if not using network module"
  type        = list(string)
  default     = []
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for deployment if not using network module"
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Standard tags for all resources"
  type        = map(any)
  default     = {
    ManagedBy = "Terraform"
  }
}