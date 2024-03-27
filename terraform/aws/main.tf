provider "aws" {
  region = var.region
}

module "terraform_state_backend" {
  source                             = "cloudposse/tfstate-backend/aws"
  version                            = "1.4.1"
  environment                        = var.environment
  name                               = "tf-state"
  namespace                          = local.namespace
  tags                               = var.tags
  terraform_backend_config_file_path = "./config"
  terraform_backend_config_file_name = "backend.tf"
  force_destroy                      = false
}

module "network" {
  count        = var.enable_network ? 1 : 0
  source       = "./modules/network"
  private_cidr = var.private_cidr
  public_cidr  = var.public_cidr
  tags         = merge(
    var.tags,
    {
      "Environment" = var.environment
    }
  )
  vpc_cidr = var.vpc_cidr
}

data "aws_subnet" "selected" {
  for_each = var.enable_network ? [] : toset(var.private_subnet_ids)
  id       = each.value
}

locals {
  namespace            = "${var.environment}-veda-auth-central"
  private_subnet_ids   = var.enable_network ? module.network[0].private_subnet_ids : var.private_subnet_ids
  private_subnet_cidrs = var.enable_network ? module.network[0].private_subnet_cidrs : [
    for s in data.aws_subnet.selected : s.cidr_block
  ]
  public_subnet_ids = var.enable_network ? module.network[0].public_subnet_ids : var.public_subnet_ids
  vpc_id            = var.enable_network ? module.network[0].vpc_id : var.vpc_id
}
