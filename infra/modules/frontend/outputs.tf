output "website_endpoint" {
  description = "Frontend S3 Website Endpoint"
  value       = module.s3_bucket.s3_bucket_website_endpoint
}
