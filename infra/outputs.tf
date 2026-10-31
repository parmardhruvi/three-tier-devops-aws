output "alb_dns_name" {
  description = "ALB DNS URL for backend"
  value       = "http://${module.backend.alb_dns_name}"
}

output "frontend_website_url" {
  description = "S3 Website URL for frontend"
  value       = "http://${module.frontend.website_endpoint}"
}
