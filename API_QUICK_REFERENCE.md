# Pluta Camera AI - API Quick Reference

## 🚀 Quick Start

### Base URL
```
http://localhost:8081/api
```

### Authentication
```http
Authorization: Bearer {your-jwt-token}
```

---

## 📹 Video Management

### Upload Video
```bash
POST /v1/video/upload
Content-Type: multipart/form-data

Parameters:
- file: video file
- zoneId: 1
- cameraId: 1
```

### Get Video Status
```bash
GET /v1/video/{videoId}/status
```

**Response:**
```json
{"status": "COMPLETED|PROCESSING|PENDING|FAILED"}
```

---

## 🎥 Stream Management

### Create Stream
```bash
POST /v1/streams
Content-Type: application/json

{
  "cameraId": 1,
  "zoneId": 1,
  "branchId": 1,
  "tenantId": 1,
  "url": "rtsp://camera.example.com/stream1",
  "samplingIntervalSeconds": 30,
  "active": true
}
```

### Activate/Deactivate Stream
```bash
PATCH /v1/streams/{id}/activate
PATCH /v1/streams/{id}/deactivate
```

---

## 📷 Camera Management

### Create Camera
```bash
POST /v1/cameras
Content-Type: application/json

{
  "name": "Main Entrance Camera",
  "code": "CAM-001",
  "zoneId": 1,
  "location": "Building A, Floor 1",
  "streamUrl": "rtsp://camera.example.com/stream1",
  "active": true
}
```

### Get Cameras by Zone
```bash
GET /v1/cameras/zone/{zoneId}
```

---

## 🖼️ Frame Analysis

### Analyze Single Frame
```bash
POST /frames/zone/{zoneId}/camera/{cameraId}/analyze
Content-Type: multipart/form-data

Parameters:
- image: image file
- confidenceThreshold: 0.2 (optional, default: 0.2)
- zoneConfidenceThreshold: 0.7 (optional, default: 0.7)
```

---

## 📊 Common Patterns

### Pagination
```bash
GET /v1/streams?page=0&size=20&sort=createdAt,desc
```

### Multi-Tenant Filtering
Most endpoints automatically filter by tenant/branch from JWT token:
```bash
GET /v1/video/by-tenant-branch
```

---

## 🔍 Response Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (successful deletion) |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Server Error |

---

## 📝 Processing Status Workflow

```
UPLOAD → PENDING → PROCESSING → COMPLETED
                              ↘ FAILED
```

1. **PENDING**: Video uploaded, waiting for processing
2. **PROCESSING**: AI analysis in progress
3. **COMPLETED**: Processing finished successfully
4. **FAILED**: Processing encountered an error

---

## 🔐 Required Roles

| Endpoint Type | Required Role |
|--------------|---------------|
| Video Upload | ADMIN, MANAGER |
| Stream Management | ADMIN |
| Camera Management | ADMIN, MANAGER |
| Frame Analysis | ADMIN, MANAGER |

---

## 🛠️ cURL Examples

### Upload and Process Video
```bash
curl -X POST "http://localhost:8081/api/v1/video/upload" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@video.mp4" \
  -F "zoneId=1" \
  -F "cameraId=1"
```

### Create Camera Stream
```bash
curl -X POST "http://localhost:8081/api/v1/streams" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cameraId": 1,
    "zoneId": 1,
    "branchId": 1,
    "tenantId": 1,
    "url": "rtsp://192.168.1.100:554/stream1",
    "samplingIntervalSeconds": 30,
    "active": true
  }'
```

### Analyze Single Frame
```bash
curl -X POST "http://localhost:8081/api/frames/zone/1/camera/1/analyze" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "image=@frame.jpg" \
  -F "confidenceThreshold=0.3" \
  -F "zoneConfidenceThreshold=0.7"
```

### Get All Videos (Paginated)
```bash
curl -X GET "http://localhost:8081/api/v1/video?page=0&size=20&sort=id,desc" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📚 Additional Resources

- **Full Documentation**: `API_DOCUMENTATION.md`
- **OpenAPI Spec**: `openapi.yaml`
- **Postman Collection**: `Pluta_Camera_AI.postman_collection.json`
- **Swagger UI**: `http://localhost:8081/api/swagger-ui/index.html`

---

## 💡 Tips

1. **File Uploads**: Always use `multipart/form-data` for video/image uploads
2. **Pagination**: Default page size is 20, max is 100
3. **Sorting**: Use format `property,direction` (e.g., `createdAt,desc`)
4. **Async Processing**: Video processing is asynchronous - poll status endpoint
5. **Security**: All passwords in requests are write-only, never returned in responses

---

## 🐛 Troubleshooting

### 401 Unauthorized
- Check if JWT token is valid and not expired
- Ensure token is included in Authorization header

### 400 Bad Request
- Validate request body against schema
- Check required fields are present
- Ensure file types are correct (video/* for videos, image/* for images)

### 404 Not Found
- Verify resource ID exists
- Check tenant/branch context

### 500 Server Error
- Check server logs
- Verify external dependencies (database, file storage)
- Ensure video processing pipeline is healthy