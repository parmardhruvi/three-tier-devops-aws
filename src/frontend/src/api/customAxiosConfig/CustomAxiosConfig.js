import axios from "axios";

const config = {
  development: {
    baseUrl: "http://localhost:8080",
  },
  production: {
    baseUrl: process.env.REACT_APP_API_BASE_URL || "http://three-tier-fe-alb-1226137904.ap-south-1.elb.amazonaws.com",
  },
}

// axios instance for making requests
const axiosInstance = axios.create({
  baseURL: config[process.env.NODE_ENV].baseUrl,
});

// request interceptor for adding token
axiosInstance.interceptors.request.use((config) => {
  // add token to request headers
  config.headers["Authorization"] = localStorage.getItem("token");
  return config;
});

export default axiosInstance;
