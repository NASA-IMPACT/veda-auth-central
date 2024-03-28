environment = "dev"
namespace = "veda-auth-central"
region      = "us-east-2"

vpc_cidr     = "10.20.30.0/24"
public_cidr  = "10.20.30.0/25"
private_cidr = "10.20.30.128/25"

alb_certificate_arn = "arn:aws:acm:us-east-2:444055461661:certificate/ee2e60b2-aee3-435c-a05a-181081bb4f7a"
keycloak_dns_name = "veda.usecustos.org"
dns_zone_id = "Z0928161AO8DT4S0II3Z"

container_cpu_units                = 1024
container_memory_limit             = 2048
container_memory_reserved          = 1024
jvm_heap_min                       = 512
jvm_heap_max                       = 1024
jvm_meta_min                       = 128
jvm_meta_max                       = 512
deployment_maximum_percent         = 100
deployment_minimum_healthy_percent = 50
desired_count                      = 1
log_retention_days                 = 5

db_instance_type         = "db.r6g.large"
db_backup_retention_days = 5
db_cluster_size          = 2
