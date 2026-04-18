# Thermal Inspection API Reference

Base URL: `http://localhost:8080`

---

## Common Rules

- All `/api/v1/**` endpoints require a valid Keycloak JWT unless stated otherwise.
- Send `Authorization: Bearer <JWT>` on every authenticated request.
- JSON request bodies require `Content-Type: application/json`.
- All successful responses are wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-04-05T12:45:11.456"
}
```

- Error responses follow the same envelope with `"success": false` and `"data": null`.
- `POST` / create endpoints return HTTP **201 Created**.
- `DELETE` endpoints return HTTP **200 OK** with `"data": null`.
- `GET /actuator/health` and `GET /actuator/info` are **public** (no JWT required).
- The **`legacy`** Spring profile enables the Annotation Log and Maintenance Record APIs.
- Fields with `null` values are omitted from all responses (`non_null` Jackson policy).

---

## 1. Transformer APIs

| Method | Path | Auth | Body | Success Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/transformers` | JWT | none | `200 ApiResponse<List<TransformerResponse>>` |
| `GET` | `/api/v1/transformers/{id}` | JWT | none | `200 ApiResponse<TransformerResponse>` |
| `POST` | `/api/v1/transformers` | JWT | `TransformerRequest` | `201 ApiResponse<TransformerResponse>` |
| `PUT` | `/api/v1/transformers/{id}` | JWT | `TransformerRequest` | `200 ApiResponse<TransformerResponse>` |
| `DELETE` | `/api/v1/transformers/{id}` | JWT | none | `200 ApiResponse<Void>` |

### TransformerRequest

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `number` | String | **Yes** | Must be unique across all transformers |
| `pole` | String | No | |
| `region` | String | No | |
| `type` | String | No | |
| `baselineImage` | String | No | S3 object key (not a signed URL) |
| `weather` | String | No | Weather condition at baseline capture time |
| `location` | String | No | |

> `baselineUploadDate` may be sent but is not persisted; the response `baselineUploadDate` always reflects the database `created_at` timestamp.

```json
{
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "weather": "Sunny",
  "location": "Gampaha"
}
```

### TransformerResponse

| Field | Type | Notes |
| --- | --- | --- |
| `id` | Long | Database primary key |
| `number` | String | |
| `pole` | String | |
| `region` | String | |
| `type` | String | |
| `baselineImage` | String | S3 object key |
| `baselineUploadDate` | String (ISO 8601) | Mapped from `created_at` |
| `weather` | String | |
| `location` | String | |
| `inspectionCount` | Integer | Number of linked inspections |

```json
{
  "id": 42,
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "baselineUploadDate": "2026-04-05T12:45:10.123",
  "weather": "Sunny",
  "location": "Gampaha",
  "inspectionCount": 0
}
```

### Create Transformer Example

`POST /api/v1/transformers`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "weather": "Sunny",
  "location": "Gampaha"
}
```

Response `201`:

```json
{
  "success": true,
  "message": "Transformer created successfully",
  "data": {
    "id": 42,
    "number": "TR-0001",
    "pole": "P-17",
    "region": "North",
    "type": "Distribution",
    "baselineImage": "transformers/base/tr-0001.jpg",
    "baselineUploadDate": "2026-04-05T12:45:10.123",
    "weather": "Sunny",
    "location": "Gampaha",
    "inspectionCount": 0
  },
  "timestamp": "2026-04-05T12:45:11.456"
}
```

---

## 2. Inspection APIs

| Method | Path | Auth | Body | Success Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/inspections/{id}` | JWT | none | `200 ApiResponse<InspectionResponse>` |
| `GET` | `/api/v1/transformers/{transformerId}/inspections` | JWT | none | `200 ApiResponse<List<InspectionResponse>>` |
| `POST` | `/api/v1/transformers/{transformerId}/inspections` | JWT | `InspectionRequest` | `201 ApiResponse<InspectionResponse>` |
| `PUT` | `/api/v1/inspections/{id}` | JWT | `InspectionRequest` | `200 ApiResponse<InspectionResponse>` |
| `DELETE` | `/api/v1/inspections/{id}` | JWT | none | `200 ApiResponse<Void>` |

### InspectionRequest

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `inspector` | String | **Yes** | Inspector's name |
| `date` | String (yyyy-MM-dd) | One of `date`/`inspectedDate` required | |
| `inspectedDate` | String (yyyy-MM-dd) | One of `date`/`inspectedDate` required | Takes priority over `date` when both are supplied |
| `notes` | String | No | Free-text notes |
| `status` | String | No | Defaults to `"SCHEDULED"` |
| `maintenanceImage` | String | No | S3 object key; takes priority over `annotatedImage` |
| `annotatedImage` | String | No | S3 object key; used when `maintenanceImage` is absent |
| `transformerId` | Long | No | Ignored on create (path param is used); applies on update |
| `maintenanceUploadDate` | String | No | Accepted but not persisted |
| `maintenanceWeather` | String | No | Accepted but not persisted |
| `anomalies` | String | No | Accepted but not persisted |
| `progressStatus` | String | No | Accepted but not persisted |

```json
{
  "date": "2026-04-05",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg"
}
```

### InspectionResponse

| Field | Type | Notes |
| --- | --- | --- |
| `id` | Long | |
| `transformerId` | Long | |
| `transformerNumber` | String | |
| `date` | String (yyyy-MM-dd) | |
| `inspectedDate` | String (yyyy-MM-dd) | Always the same value as `date` |
| `inspector` | String | |
| `notes` | String | |
| `status` | String | |
| `maintenanceImage` | String | S3 object key |
| `annotatedImage` | String | Same value as `maintenanceImage` |
| `imageLevelLabel` | String | `null` until `/analyze` is run; then `"Normal"`, `"Potentially Faulty"`, or `"Faulty"` |
| `anomalyCount` | Integer | `null` until `/analyze` is run |

```json
{
  "id": 101,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "date": "2026-04-05",
  "inspectedDate": "2026-04-05",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "annotatedImage": "inspections/maintenance/inspect-001.jpg",
  "imageLevelLabel": "Potentially Faulty",
  "anomalyCount": 2
}
```

### Create Inspection Example

`POST /api/v1/transformers/42/inspections`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "date": "2026-04-05",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg"
}
```

Response `201`:

```json
{
  "success": true,
  "message": "Inspection created successfully",
  "data": {
    "id": 101,
    "transformerId": 42,
    "transformerNumber": "TR-0001",
    "date": "2026-04-05",
    "inspectedDate": "2026-04-05",
    "inspector": "Nimal Perera",
    "notes": "Routine inspection completed.",
    "status": "COMPLETED",
    "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
    "annotatedImage": "inspections/maintenance/inspect-001.jpg"
  },
  "timestamp": "2026-04-05T12:50:00.000"
}
```

> `imageLevelLabel` and `anomalyCount` are absent in the response above because the inspection has not been analyzed yet. They appear after `POST /api/v1/inspections/{id}/analyze` is called.

---

## 3. Anomaly / Annotation APIs

| Method | Path | Auth | Body | Success Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/inspections/{inspectionId}/anomalies` | JWT | none | `200 ApiResponse<List<AnnotationResponse>>` |
| `POST` | `/api/v1/inspections/{inspectionId}/anomalies` | JWT | `AnnotationRequest` | `201 ApiResponse<AnnotationResponse>` |
| `PUT` | `/api/v1/anomalies/{id}` | JWT | `AnnotationRequest` | `200 ApiResponse<AnnotationResponse>` |
| `DELETE` | `/api/v1/anomalies/{id}` | JWT | none | `200 ApiResponse<Void>` |

### AnnotationRequest

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `x` | Double | **Yes** | Left edge of bounding box |
| `y` | Double | **Yes** | Top edge of bounding box |
| `w` | Double | **Yes** | Width of bounding box |
| `h` | Double | **Yes** | Height of bounding box |
| `classification` | String | No | Preferred label; falls back to `severity` when absent |
| `severity` | String | No | Used as label only when `classification` is absent |
| `confidence` | Double | No | 0.0 – 1.0 |
| `comment` | String | No | Human notes |
| `source` | String | No | `"user"` (default) or `"ai"`. When `"user"` or absent, annotation is marked as manually verified. |
| `userId` | String | No | Local user id as a string; if parseable as Long, links the annotation to that user |
| `annotationId` | String | No | Client-side identifier; accepted but not persisted |
| `deleted` | Boolean | No | Accepted but has no effect; response always returns `false` |

```json
{
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.94,
  "severity": "Potentially Faulty",
  "classification": "PointOverload",
  "comment": "Visible heat spot.",
  "source": "user",
  "userId": "12"
}
```

### AnnotationResponse

AI-generated annotations (created by `/analyze`) carry full detection metadata. Manually created annotations leave those fields absent.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | Long | Database primary key |
| `inspectionId` | Long | |
| `annotationId` | String | Stringified database `id` |
| `x`, `y`, `w`, `h` | Double | Bounding box coordinates |
| `confidence` | Double | 0.0 – 1.0 |
| `severity` | String | `"Normal"`, `"Potentially Faulty"`, `"Faulty"`, or custom label |
| `severityScore` | Double | 0.0 – 100.0; null for manual annotations |
| `classification` | String | `"LooseJoint"`, `"PointOverload"`, `"FullWireOverload"`, `"None"`, or custom label |
| `area` | Integer | Blob area in pixels; null for manual annotations |
| `centroid` | `{x, y}` | Centroid coordinates; null for manual annotations |
| `meanDeltaE` | Double | Mean CIELAB ΔE; null for manual annotations |
| `peakDeltaE` | Double | Peak CIELAB ΔE; null for manual annotations |
| `meanHsv` | `{h, s, v}` | Mean HSV values; null for manual annotations |
| `elongation` | Double | Major/minor axis ratio; null for manual annotations |
| `comment` | String | Human notes |
| `source` | String | `"user"` if manually created; `"ai"` if generated by `/analyze` |
| `deleted` | Boolean | Always `false` |
| `userId` | String | Linked local user id; null for AI annotations |
| `createdAt` | String (ISO 8601) | |
| `updatedAt` | String (ISO 8601) | Same as `createdAt` |

AI-generated annotation example:

```json
{
  "id": 55,
  "inspectionId": 101,
  "annotationId": "55",
  "x": 142.0,
  "y": 87.0,
  "w": 38.0,
  "h": 44.0,
  "confidence": 0.82,
  "severity": "Potentially Faulty",
  "severityScore": 54.3,
  "classification": "PointOverload",
  "area": 892,
  "centroid": { "x": 161.4, "y": 109.2 },
  "meanDeltaE": 11.3,
  "peakDeltaE": 17.8,
  "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
  "elongation": 1.4,
  "source": "ai",
  "deleted": false,
  "createdAt": "2026-04-05T12:55:00.000",
  "updatedAt": "2026-04-05T12:55:00.000"
}
```

### Create Annotation Example

`POST /api/v1/inspections/101/anomalies`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.94,
  "classification": "PointOverload",
  "comment": "Visible heat spot.",
  "source": "user",
  "userId": "12"
}
```

Response `201`:

```json
{
  "success": true,
  "message": "Anomaly created successfully",
  "data": {
    "id": 55,
    "inspectionId": 101,
    "annotationId": "55",
    "x": 128.5,
    "y": 92.0,
    "w": 44.0,
    "h": 38.5,
    "confidence": 0.94,
    "classification": "PointOverload",
    "comment": "Visible heat spot.",
    "source": "user",
    "deleted": false,
    "userId": "12",
    "createdAt": "2026-04-05T12:55:00.000",
    "updatedAt": "2026-04-05T12:55:00.000"
  },
  "timestamp": "2026-04-05T12:55:01.000"
}
```

---

## 4. Image APIs

| Method | Path | Auth | Query Params | Success Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/images/generate-upload-url` | JWT | `folder` (req), `extension` (req) | `200 ApiResponse<PresignedUploadResponse>` |
| `GET` | `/api/v1/images/generate-download-url` | JWT | `key` (req) | `200 ApiResponse<String>` |

### generate-upload-url

Generates a pre-signed **PUT** URL for direct client-to-S3 upload. The URL is valid for **15 minutes**.

| Query Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `folder` | String | **Yes** | S3 folder prefix, e.g. `transformers/base` or `inspections/maintenance` |
| `extension` | String | **Yes** | File extension without dot, e.g. `jpg`, `png` |

#### PresignedUploadResponse

| Field | Type | Notes |
| --- | --- | --- |
| `uploadUrl` | String | Pre-signed S3 PUT URL. Use this to upload the file with `PUT`. Do **not** send this URL to any backend endpoint. |
| `objectKey` | String | S3 object key. Persist this value in `TransformerRequest.baselineImage` or `InspectionRequest.maintenanceImage`. |

```json
{
  "uploadUrl": "https://bucket.s3.amazonaws.com/transformers/base/uuid.jpg?X-Amz-...",
  "objectKey": "transformers/base/uuid.jpg"
}
```

Example request:

`GET /api/v1/images/generate-upload-url?folder=transformers/base&extension=jpg`

```json
{
  "success": true,
  "message": "Pre-signed upload URL generated",
  "data": {
    "uploadUrl": "https://bucket.s3.amazonaws.com/transformers/base/uuid.jpg?X-Amz-...",
    "objectKey": "transformers/base/uuid.jpg"
  },
  "timestamp": "2026-04-05T12:45:00.000"
}
```

### generate-download-url

Generates a pre-signed **GET** URL for a private S3 object. The URL is valid for **60 minutes**.

| Query Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `key` | String | **Yes** | S3 object key stored in the database |

The `data` field is the pre-signed URL string directly.

`GET /api/v1/images/generate-download-url?key=transformers/base/uuid.jpg`

```json
{
  "success": true,
  "message": "Pre-signed download URL generated",
  "data": "https://bucket.s3.amazonaws.com/transformers/base/uuid.jpg?X-Amz-...",
  "timestamp": "2026-04-05T12:46:00.000"
}
```

---

## 5. Anomaly Detection API

| Method | Path | Auth | Body | Success Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/inspections/{id}/analyze` | JWT | `InspectionAnalysisRequest` (optional) | `200 ApiResponse<AnomalyDetectionResponse>` |

This endpoint acts as a gateway to the anomaly detection microservice. It:

1. Loads the inspection record and its parent transformer from the database.
2. Generates pre-signed S3 download URLs for both the transformer's baseline image and the inspection's maintenance image.
3. Sends those URLs (plus optional `slider_percent`) to the detection service via `POST /api/v1/detect`.
4. Persists the detection results: updates `imageLevelLabel`, `anomalyCount`, `detectionRequestId`, and `detectionMetrics` on the inspection row; deletes all previous AI-generated anomalies for that inspection; saves the new anomaly blobs.
5. Returns the full detection response.

**Error cases:**

| HTTP | Condition |
| --- | --- |
| `400 Bad Request` | Inspection has no `maintenanceImage` key set, or transformer has no `baselineImage` key set |
| `500 Internal Server Error` | The detection microservice returned an error or the request timed out |

### InspectionAnalysisRequest

The body is entirely optional. Omit it or send `{}` when no slider value is needed.

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `sliderPercent` | Double | No | Threshold sensitivity. Range `0.0 – 100.0`. `0` = least sensitive; `100` = most sensitive. Omit for adaptive SSIM-based defaults. |

```json
{
  "sliderPercent": 15.0
}
```

### AnomalyDetectionResponse

The full response from the detection microservice is forwarded and wrapped in `ApiResponse`. Re-running `/analyze` replaces all previous AI-detected anomalies for that inspection.

| Field | Type | Notes |
| --- | --- | --- |
| `requestId` | String (UUID) | Unique ID for this detection run |
| `timestamp` | String (ISO 8601) | UTC timestamp from the detection service |
| `imageLevelLabel` | String | `"Normal"`, `"Potentially Faulty"`, or `"Faulty"` |
| `anomalyCount` | Integer | Total anomaly blobs detected |
| `anomalies` | `DetectedAnomaly[]` | Per-blob details (see below) |
| `metrics` | `DetectionMetrics` | Pipeline diagnostics (see below) |

#### DetectedAnomaly

| Field | Type | Notes |
| --- | --- | --- |
| `id` | String | Sequential id, e.g. `"anomaly_1"` |
| `bbox.x` | Integer | Left edge (pixels) |
| `bbox.y` | Integer | Top edge (pixels) |
| `bbox.width` | Integer | Width (pixels) |
| `bbox.height` | Integer | Height (pixels) |
| `confidence` | Double | 0.0 – 1.0 |
| `severity` | String | `"Normal"`, `"Potentially Faulty"`, or `"Faulty"` |
| `severityScore` | Double | 0.0 – 100.0 |
| `classification` | String | `"LooseJoint"`, `"PointOverload"`, `"FullWireOverload"`, or `"None"` |
| `area` | Integer | Blob area in pixels |
| `centroid.x` | Double | Centroid X (pixels) |
| `centroid.y` | Double | Centroid Y (pixels) |
| `meanDeltaE` | Double | Mean CIELAB ΔE |
| `peakDeltaE` | Double | Peak CIELAB ΔE |
| `meanHsv.h` | Double | Mean Hue (0 – 180, OpenCV scale) |
| `meanHsv.s` | Double | Mean Saturation (0 – 255) |
| `meanHsv.v` | Double | Mean Value/Brightness (0 – 255) |
| `elongation` | Double | Major/minor axis ratio |

#### DetectionMetrics

| Field | Type | Notes |
| --- | --- | --- |
| `meanSsim` | Double | Structural Similarity Index between aligned images |
| `warpModel` | String | `"homography"` or `"affine"` |
| `warpSuccess` | Boolean | Whether ECC alignment converged |
| `warpScore` | Double | ECC convergence score |
| `thresholdPotential` | Double | Final ΔE threshold for "Potentially Faulty" |
| `thresholdFault` | Double | Final ΔE threshold for "Faulty" |
| `basePotential` | Double | SSIM-adaptive base before slider adjustment |
| `baseFault` | Double | SSIM-adaptive base before slider adjustment |
| `sliderPercent` | Double | Passed-in value, or `null` |
| `scaleApplied` | Double | Computed scale factor, or `null` |
| `thresholdSource` | String | `"adaptive_ssim"`, `"slider_scaled"`, `"adaptive_ssim+palette_soften"`, or `"slider_scaled+palette_soften"` |
| `ratio` | Double | `thresholdFault / thresholdPotential` |

### Analyze Inspection Example

`POST /api/v1/inspections/101/analyze`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "sliderPercent": 15.0
}
```

Response `200`:

```json
{
  "success": true,
  "message": "Inspection analysis completed",
  "data": {
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "timestamp": "2026-04-05T10:23:45.123456",
    "imageLevelLabel": "Potentially Faulty",
    "anomalyCount": 2,
    "anomalies": [
      {
        "id": "anomaly_1",
        "bbox": { "x": 142, "y": 87, "width": 38, "height": 44 },
        "confidence": 0.82,
        "severity": "Potentially Faulty",
        "severityScore": 54.3,
        "classification": "PointOverload",
        "area": 892,
        "centroid": { "x": 161.4, "y": 109.2 },
        "meanDeltaE": 11.3,
        "peakDeltaE": 17.8,
        "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
        "elongation": 1.4
      }
    ],
    "metrics": {
      "meanSsim": 0.874,
      "warpModel": "homography",
      "warpSuccess": true,
      "warpScore": 0.021,
      "thresholdPotential": 8.0,
      "thresholdFault": 12.0,
      "basePotential": 8.0,
      "baseFault": 12.0,
      "sliderPercent": 15.0,
      "scaleApplied": 1.15,
      "thresholdSource": "slider_scaled",
      "ratio": 1.5
    }
  },
  "timestamp": "2026-04-05T10:23:46.000"
}
```

---

## 6. Legacy Profile APIs

These endpoints are **only active** when the application starts with the `legacy` Spring profile (`--spring.profiles.active=legacy`). They are not available in the default profile.

### Annotation Logs

| Method | Path | Auth | Notes | Success Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/annotation-logs` | JWT | Returns all logs | `200 ApiResponse<List<AnnotationLogResponse>>` |
| `GET` | `/api/v1/annotation-logs/inspection/{inspectionId}` | JWT | Returns logs for one inspection | `200 ApiResponse<List<AnnotationLogResponse>>` |
| `GET` | `/api/v1/annotation-logs/export/json` | JWT | File download | `200` `Content-Type: application/json`, `Content-Disposition: attachment; filename=annotation_logs.json` |
| `GET` | `/api/v1/annotation-logs/export/csv` | JWT | File download | `200` `Content-Type: text/csv`, `Content-Disposition: attachment; filename=annotation_logs.csv` |

#### AnnotationLogResponse

| Field | Type |
| --- | --- |
| `id` | Long |
| `inspectionId` | Long |
| `transformerId` | Long |
| `transformerNumber` | String |
| `imageId` | String |
| `actionType` | String |
| `annotationData` | String (JSON string) |
| `aiPrediction` | String |
| `userAnnotation` | String |
| `userId` | String |
| `timestamp` | String (ISO 8601) |
| `notes` | String |

```json
{
  "id": 9,
  "inspectionId": 101,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "imageId": "img-001",
  "actionType": "CREATE",
  "annotationData": "{\"x\":128,\"y\":92}",
  "aiPrediction": "PointOverload",
  "userAnnotation": "PointOverload",
  "userId": "12",
  "timestamp": "2026-04-05T12:55:00.000",
  "notes": "Initial annotation saved"
}
```

### Maintenance Records

| Method | Path | Auth | Body / Params | Success Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/records` | JWT | optional query `transformer_id` | `200 ApiResponse<List<MaintenanceRecordResponse>>` |
| `GET` | `/api/v1/records/{id}` | JWT | path `id` | `200 ApiResponse<MaintenanceRecordResponse>` |
| `GET` | `/api/v1/records/transformer/{transformerId}` | JWT | path `transformerId` | `200 ApiResponse<List<MaintenanceRecordResponse>>` |
| `POST` | `/api/v1/records` | JWT | `MaintenanceRecordRequest` | `201 ApiResponse<MaintenanceRecordResponse>` |
| `PUT` | `/api/v1/records/{id}` | JWT | path `id`, `MaintenanceRecordRequest` | `200 ApiResponse<MaintenanceRecordResponse>` |
| `DELETE` | `/api/v1/records/{id}` | JWT | path `id` | `200 ApiResponse<Void>` |
| `GET` | `/api/v1/records/export/pdf/{id}` | JWT | path `id` | `200` `Content-Type: application/pdf`, `Content-Disposition: attachment; filename=maintenance_record_{id}.pdf` |

#### MaintenanceRecordRequest

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `transformerId` | Long | **Yes** | |
| `inspectionId` | Long | No | |
| `engineerName` | String | No | |
| `status` | String | No | |
| `readings` | String | No | |
| `recommendedAction` | String | No | |
| `notes` | String | No | |
| `annotatedImage` | String | No | S3 object key |
| `anomalies` | String | No | JSON string |
| `location` | String | No | |

```json
{
  "transformerId": 42,
  "inspectionId": 101,
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha"
}
```

#### MaintenanceRecordResponse

| Field | Type |
| --- | --- |
| `id` | Long |
| `transformerId` | Long |
| `transformerNumber` | String |
| `inspectionId` | Long |
| `recordTimestamp` | String (ISO 8601) |
| `engineerName` | String |
| `status` | String |
| `readings` | String |
| `recommendedAction` | String |
| `notes` | String |
| `annotatedImage` | String |
| `anomalies` | String |
| `location` | String |
| `createdAt` | String (ISO 8601) |
| `updatedAt` | String (ISO 8601) |

```json
{
  "id": 77,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "inspectionId": 101,
  "recordTimestamp": "2026-04-05T13:10:00",
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha",
  "createdAt": "2026-04-05T13:10:00",
  "updatedAt": "2026-04-05T13:10:00"
}
```

---

## 7. Public Actuator Endpoints

| Method | Path | Auth | Response |
| --- | --- | --- | --- |
| `GET` | `/actuator/health` | None | `{ "status": "UP" }` |
| `GET` | `/actuator/info` | None | `{ "app": { "name": "thermal-inspection-backend", "version": "1.0.0" } }` |

---

## Frontend Image Upload Flow

1. `GET /api/v1/images/generate-upload-url?folder=transformers/base&extension=jpg`
2. `PUT <data.uploadUrl>` — upload the file directly from the browser to S3.
3. Copy `data.objectKey` from the response.
4. Use `objectKey` as the `baselineImage` field in `TransformerRequest` or `maintenanceImage` in `InspectionRequest`.
5. Call the relevant create/update endpoint.

> Never send the pre-signed `uploadUrl` to this backend as an image reference. Always use `objectKey`.

---

## Error Response Format

```json
{
  "success": false,
  "message": "Resource not found: Inspection with id 999",
  "timestamp": "2026-04-05T12:45:11.456"
}
```

| HTTP Status | Typical Cause |
| --- | --- |
| `400 Bad Request` | Validation failure, missing required field, or missing image key on `/analyze` |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | Duplicate unique field (e.g. transformer number) |
| `500 Internal Server Error` | Unhandled exception or detection microservice failure |


Base URL: `http://localhost:8080`

## Common Rules

- All `/api/v1/**` endpoints require a JWT unless noted otherwise.
- Send `Authorization: Bearer <JWT>` and `Content-Type: application/json` for JSON requests.
- Most successful responses are wrapped in `ApiResponse`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-03-31T12:45:11.456"
}
```

- Create endpoints usually return HTTP `201 Created`.
- Delete endpoints return HTTP `200 OK` with `data: null`.
- `GET /actuator/health` and `GET /actuator/info` are public.
- The `legacy` profile enables the maintenance record and annotation log APIs.

## 1. Transformer APIs

All transformer APIs use the `TransformerRequest` and `TransformerResponse` shapes.

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/transformers` | none | `ApiResponse<List<TransformerResponse>>` |
| `GET` | `/api/v1/transformers/{id}` | path `id` | `ApiResponse<TransformerResponse>` |
| `POST` | `/api/v1/transformers` | `TransformerRequest` | `ApiResponse<TransformerResponse>` |
| `PUT` | `/api/v1/transformers/{id}` | path `id`, `TransformerRequest` | `ApiResponse<TransformerResponse>` |
| `DELETE` | `/api/v1/transformers/{id}` | path `id` | `ApiResponse<Void>` |

### TransformerRequest

`number` is required. `baselineImage` should be the S3 object key returned by the upload URL API, not the signed URL.

```json
{
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "baselineUploadDate": "2026-03-31",
  "weather": "Sunny",
  "location": "Gampaha"
}
```

### TransformerResponse

The backend maps the entity `createdAt` value into `baselineUploadDate`.

```json
{
  "id": 42,
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "baselineUploadDate": "2026-03-31T12:45:10.123",
  "weather": "Sunny",
  "location": "Gampaha",
  "inspectionCount": 0
}
```

### Create Transformer Example

`POST /api/v1/transformers`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "weather": "Sunny",
  "location": "Gampaha"
}
```

Response:

```json
{
  "success": true,
  "message": "Transformer created successfully",
  "data": {
    "id": 42,
    "number": "TR-0001",
    "pole": "P-17",
    "region": "North",
    "type": "Distribution",
    "baselineImage": "transformers/base/tr-0001.jpg",
    "baselineUploadDate": "2026-03-31T12:45:10.123",
    "weather": "Sunny",
    "location": "Gampaha",
    "inspectionCount": 0
  },
  "timestamp": "2026-03-31T12:45:11.456"
}
```

### List Transformers Example

`GET /api/v1/transformers`

```json
{
  "success": true,
  "message": "Transformers retrieved successfully",
  "data": [
    {
      "id": 42,
      "number": "TR-0001",
      "pole": "P-17",
      "region": "North",
      "type": "Distribution",
      "baselineImage": "transformers/base/tr-0001.jpg",
      "baselineUploadDate": "2026-03-31T12:45:10.123",
      "weather": "Sunny",
      "location": "Gampaha",
      "inspectionCount": 0
    }
  ],
  "timestamp": "2026-03-31T12:45:11.456"
}
```

## 2. Inspection APIs

All inspection APIs use the `InspectionRequest` and `InspectionResponse` shapes.

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/inspections/{id}` | path `id` | `ApiResponse<InspectionResponse>` |
| `GET` | `/api/v1/transformers/{transformerId}/inspections` | path `transformerId` | `ApiResponse<List<InspectionResponse>>` |
| `POST` | `/api/v1/transformers/{transformerId}/inspections` | path `transformerId`, `InspectionRequest` | `ApiResponse<InspectionResponse>` |
| `PUT` | `/api/v1/inspections/{id}` | path `id`, `InspectionRequest` | `ApiResponse<InspectionResponse>` |
| `DELETE` | `/api/v1/inspections/{id}` | path `id` | `ApiResponse<Void>` |

### InspectionRequest

`inspector` is required. On create, the path parameter `transformerId` is the real transformer link; the `transformerId` field in the body is optional. The backend accepts `date` or `inspectedDate` in `yyyy-MM-dd` format.

```json
{
  "date": "2026-03-31",
  "inspectedDate": "2026-03-31",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "maintenanceUploadDate": "2026-03-31",
  "maintenanceWeather": "Sunny",
  "annotatedImage": "inspections/annotated/inspect-001.jpg",
  "anomalies": "[]",
  "progressStatus": "DONE"
}
```

### InspectionResponse

The service returns the inspection date as both `date` and `inspectedDate`, and it maps the stored inspection image key to both `maintenanceImage` and `annotatedImage`.

```json
{
  "id": 101,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "date": "2026-03-31",
  "inspectedDate": "2026-03-31",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "annotatedImage": "inspections/maintenance/inspect-001.jpg"
}
```

### Create Inspection Example

`POST /api/v1/transformers/42/inspections`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "date": "2026-03-31",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "progressStatus": "DONE"
}
```

Response:

```json
{
  "success": true,
  "message": "Inspection created successfully",
  "data": {
    "id": 101,
    "transformerId": 42,
    "transformerNumber": "TR-0001",
    "date": "2026-03-31",
    "inspectedDate": "2026-03-31",
    "inspector": "Nimal Perera",
    "notes": "Routine inspection completed.",
    "status": "COMPLETED",
    "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
    "annotatedImage": "inspections/maintenance/inspect-001.jpg"
  },
  "timestamp": "2026-03-31T12:50:00.000"
}
```

## 3. Anomaly / Annotation APIs

These endpoints use the `AnnotationRequest` and `AnnotationResponse` shapes, even though the route names still use `anomalies`.

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/inspections/{inspectionId}/anomalies` | path `inspectionId` | `ApiResponse<List<AnnotationResponse>>` |
| `POST` | `/api/v1/inspections/{inspectionId}/anomalies` | path `inspectionId`, `AnnotationRequest` | `ApiResponse<AnnotationResponse>` |
| `PUT` | `/api/v1/anomalies/{id}` | path `id`, `AnnotationRequest` | `ApiResponse<AnnotationResponse>` |
| `DELETE` | `/api/v1/anomalies/{id}` | path `id` | `ApiResponse<Void>` |

### AnnotationRequest

`x`, `y`, `w`, and `h` are required. `classification` is preferred when present; otherwise the backend falls back to `severity`.

```json
{
  "annotationId": "A-001",
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.94,
  "severity": "HIGH",
  "classification": "CRACK",
  "comment": "Insulator crack observed.",
  "source": "ai",
  "deleted": false,
  "userId": "12"
}
```

### AnnotationResponse

The backend derives `annotationId` from the database id when needed, and it always returns `deleted: false` in the current mapping. AI-generated annotations (produced by the `/analyze` endpoint) populate the extra detection fields; manually created annotations leave those fields `null`.

```json
{
  "id": 55,
  "inspectionId": 101,
  "annotationId": "55",
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.82,
  "severity": "Potentially Faulty",
  "severityScore": 54.3,
  "classification": "PointOverload",
  "area": 892,
  "centroid": { "x": 161.4, "y": 109.2 },
  "meanDeltaE": 11.3,
  "peakDeltaE": 17.8,
  "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
  "elongation": 1.4,
  "comment": null,
  "source": "ai",
  "deleted": false,
  "userId": null,
  "createdAt": "2026-03-31T12:55:00.000",
  "updatedAt": "2026-03-31T12:55:00.000"
}
```

### Create Annotation Example

`POST /api/v1/inspections/101/anomalies`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.94,
  "severity": "HIGH",
  "classification": "CRACK",
  "comment": "Insulator crack observed.",
  "source": "ai",
  "userId": "12"
}
```

Response:

```json
{
  "success": true,
  "message": "Anomaly created successfully",
  "data": {
    "id": 55,
    "inspectionId": 101,
    "annotationId": "55",
    "x": 128.5,
    "y": 92.0,
    "w": 44.0,
    "h": 38.5,
    "confidence": 0.94,
    "severity": "HIGH",
    "severityScore": null,
    "classification": "CRACK",
    "area": null,
    "centroid": null,
    "meanDeltaE": null,
    "peakDeltaE": null,
    "meanHsv": null,
    "elongation": null,
    "comment": "Insulator crack observed.",
    "source": "ai",
    "deleted": false,
    "userId": "12",
    "createdAt": "2026-03-31T12:55:00.000",
    "updatedAt": "2026-03-31T12:55:00.000"
  },
  "timestamp": "2026-03-31T12:55:01.000"
}
```

## 4. Image APIs

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/images/generate-upload-url` | query `folder`, query `extension` | `ApiResponse<PresignedUploadResponse>` |
| `GET` | `/api/v1/images/generate-download-url` | query `key` | `ApiResponse<String>` |

### PresignedUploadResponse

Use `uploadUrl` for the direct `PUT` to S3. Store `objectKey` in the backend as the image reference.

```json
{
  "uploadUrl": "https://s3.amazonaws.com/...signed-url...",
  "objectKey": "transformers/base/abc123.jpg"
}
```

### Generate Upload URL Example

`GET /api/v1/images/generate-upload-url?folder=transformers/base&extension=jpg`

```json
{
  "success": true,
  "message": "Pre-signed upload URL generated",
  "data": {
    "uploadUrl": "https://s3.amazonaws.com/...signed-url...",
    "objectKey": "transformers/base/abc123.jpg"
  },
  "timestamp": "2026-03-31T12:45:00.000"
}
```

### Generate Download URL Example

`GET /api/v1/images/generate-download-url?key=transformers/base/abc123.jpg`

```json
{
  "success": true,
  "message": "Pre-signed download URL generated",
  "data": "https://s3.amazonaws.com/...signed-url...",
  "timestamp": "2026-03-31T12:46:00.000"
}
```

## 5. Anomaly Detection API

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `POST` | `/api/v1/inspections/{id}/analyze` | optional `InspectionAnalysisRequest` | `ApiResponse<AnomalyDetectionResponse>` |

### InspectionAnalysisRequest

The body is optional. If present, it only contains `sliderPercent`.

```json
{
  "sliderPercent": 15.0
}
```

### AnomalyDetectionResponse

The full response from the detection microservice is forwarded by this backend and wrapped in `ApiResponse`. The `anomalies` array is also persisted to the database — re-running `/analyze` replaces all previous AI-detected anomalies for that inspection.

```json
{
  "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "timestamp": "2026-04-05T10:23:45.123456",
  "imageLevelLabel": "Potentially Faulty",
  "anomalyCount": 2,
  "anomalies": [
    {
      "id": "anomaly_1",
      "bbox": {
        "x": 142,
        "y": 87,
        "width": 38,
        "height": 44
      },
      "confidence": 0.82,
      "severity": "Potentially Faulty",
      "severityScore": 54.3,
      "classification": "PointOverload",
      "area": 892,
      "centroid": { "x": 161.4, "y": 109.2 },
      "meanDeltaE": 11.3,
      "peakDeltaE": 17.8,
      "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
      "elongation": 1.4
    }
  ],
  "metrics": {
    "meanSsim": 0.874,
    "warpModel": "homography",
    "warpSuccess": true,
    "warpScore": 0.021,
    "thresholdPotential": 8.0,
    "thresholdFault": 12.0,
    "basePotential": 8.0,
    "baseFault": 12.0,
    "sliderPercent": 15.0,
    "scaleApplied": 1.15,
    "thresholdSource": "slider_scaled",
    "ratio": 1.5
  }
}
```

### Analyze Inspection Example

`POST /api/v1/inspections/101/analyze`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "sliderPercent": 15.0
}
```

Response:

```json
{
  "success": true,
  "message": "Inspection analysis completed",
  "data": {
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "timestamp": "2026-04-05T10:23:45.123456",
    "imageLevelLabel": "Potentially Faulty",
    "anomalyCount": 2,
    "anomalies": [
      {
        "id": "anomaly_1",
        "bbox": {
          "x": 142,
          "y": 87,
          "width": 38,
          "height": 44
        },
        "confidence": 0.82,
        "severity": "Potentially Faulty",
        "severityScore": 54.3,
        "classification": "PointOverload",
        "area": 892,
        "centroid": { "x": 161.4, "y": 109.2 },
        "meanDeltaE": 11.3,
        "peakDeltaE": 17.8,
        "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
        "elongation": 1.4
      }
    ],
    "metrics": {
      "meanSsim": 0.874,
      "warpModel": "homography",
      "warpSuccess": true,
      "warpScore": 0.021,
      "thresholdPotential": 8.0,
      "thresholdFault": 12.0,
      "basePotential": 8.0,
      "baseFault": 12.0,
      "sliderPercent": 15.0,
      "scaleApplied": 1.15,
      "thresholdSource": "slider_scaled",
      "ratio": 1.5
    }
  },
  "timestamp": "2026-04-05T10:23:46.000"
}
```

## 6. Legacy Profile APIs

These controllers are only active when the app runs with the `legacy` Spring profile.

### Annotation Logs

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/annotation-logs` | none | `ApiResponse<List<AnnotationLogResponse>>` |
| `GET` | `/api/v1/annotation-logs/inspection/{inspectionId}` | path `inspectionId` | `ApiResponse<List<AnnotationLogResponse>>` |
| `GET` | `/api/v1/annotation-logs/export/json` | none | raw JSON string download |
| `GET` | `/api/v1/annotation-logs/export/csv` | none | raw CSV string download |

#### AnnotationLogResponse

```json
{
  "id": 9,
  "inspectionId": 101,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "imageId": "img-001",
  "actionType": "CREATE",
  "annotationData": "{\"x\":128,\"y\":92}",
  "aiPrediction": "CRACK",
  "userAnnotation": "CRACK",
  "userId": "12",
  "timestamp": "2026-03-31T12:55:00.000",
  "notes": "Initial annotation saved"
}
```

#### Example

`GET /api/v1/annotation-logs`

```json
{
  "success": true,
  "message": "Annotation logs retrieved successfully",
  "data": [
    {
      "id": 9,
      "inspectionId": 101,
      "transformerId": 42,
      "transformerNumber": "TR-0001",
      "imageId": "img-001",
      "actionType": "CREATE",
      "annotationData": "{\"x\":128,\"y\":92}",
      "aiPrediction": "CRACK",
      "userAnnotation": "CRACK",
      "userId": "12",
      "timestamp": "2026-03-31T12:55:00.000",
      "notes": "Initial annotation saved"
    }
  ],
  "timestamp": "2026-03-31T13:05:00.000"
}
```

### Maintenance Records

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/records` | optional query `transformer_id` | `ApiResponse<List<MaintenanceRecordResponse>>` |
| `GET` | `/api/v1/records/{id}` | path `id` | `ApiResponse<MaintenanceRecordResponse>` |
| `GET` | `/api/v1/records/transformer/{transformerId}` | path `transformerId` | `ApiResponse<List<MaintenanceRecordResponse>>` |
| `POST` | `/api/v1/records` | `MaintenanceRecordRequest` | `ApiResponse<MaintenanceRecordResponse>` |
| `PUT` | `/api/v1/records/{id}` | path `id`, `MaintenanceRecordRequest` | `ApiResponse<MaintenanceRecordResponse>` |
| `DELETE` | `/api/v1/records/{id}` | path `id` | `ApiResponse<Void>` |
| `GET` | `/api/v1/records/export/pdf/{id}` | path `id` | PDF file download |

#### MaintenanceRecordRequest

`transformerId` is required. `inspectionId` is optional.

```json
{
  "transformerId": 42,
  "inspectionId": 101,
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha"
}
```

#### MaintenanceRecordResponse

```json
{
  "id": 77,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "inspectionId": 101,
  "recordTimestamp": "2026-03-31T13:10:00",
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha",
  "createdAt": "2026-03-31T13:10:00",
  "updatedAt": "2026-03-31T13:10:00"
}
```

#### Example

`POST /api/v1/records`

```json
{
  "transformerId": 42,
  "inspectionId": 101,
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha"
}
```

```json
{
  "success": true,
  "message": "Record created successfully",
  "data": {
    "id": 77,
    "transformerId": 42,
    "transformerNumber": "TR-0001",
    "inspectionId": 101,
    "recordTimestamp": "2026-03-31T13:10:00",
    "engineerName": "Nimal Perera",
    "status": "COMPLETED",
    "readings": "Voltage stable. Temperature normal.",
    "recommendedAction": "Continue monitoring",
    "notes": "No major issues found.",
    "annotatedImage": "records/annotated/record-001.jpg",
    "anomalies": "[]",
    "location": "Gampaha",
    "createdAt": "2026-03-31T13:10:00",
    "updatedAt": "2026-03-31T13:10:00"
  },
  "timestamp": "2026-03-31T13:10:01.000"
}
```

### Public Actuator Endpoints

| Method | Path | Notes |
| --- | --- | --- |
| `GET` | `/actuator/health` | public, returns `{ "status": "UP" }` when healthy |
| `GET` | `/actuator/info` | public, returns app metadata |

## Frontend Flow for Image Uploads

1. Call `GET /api/v1/images/generate-upload-url`.
2. Use `data.uploadUrl` to `PUT` the file directly to S3.
3. Save `data.objectKey` into the transformer or inspection payload.
4. Call the relevant create/update API.

## Important Notes

- Do not send the pre-signed `uploadUrl` to the backend as an image reference.
- Use the `objectKey` instead.
- For inspection creation, the backend uses the path transformer id and only requires `inspector` plus a date field in `yyyy-MM-dd` format.
- For anomaly creation, the required coordinates are `x`, `y`, `w`, and `h`.

## To run

- .\mvnw.cmd spring-boot:run
