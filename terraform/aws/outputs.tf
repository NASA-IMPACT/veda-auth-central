output "public_subnet_cidrs" {
  value = var.enable_network ? module.network[0].public_subnet_cidrs : []
}

output "private_subnet_cidrs" {
  value = var.enable_network ? module.network[0].private_subnet_cidrs : local.private_subnet_cidrs
}

output "state_table" {
  value = module.terraform_state_backend.dynamodb_table_name
}

output "state_bucket" {
  value = module.terraform_state_backend.s3_bucket_domain_name
}
