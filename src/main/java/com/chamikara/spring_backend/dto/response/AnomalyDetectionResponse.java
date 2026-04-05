package com.chamikara.spring_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyDetectionResponse {

    private String requestId;
    private String timestamp;
    private String imageLevelLabel;
    private Integer anomalyCount;
    private List<DetectedAnomaly> anomalies;
    private DetectionMetrics metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetectedAnomaly {
        private String id;
        private BoundingBox bbox;
        private Double confidence;
        private String severity;
        private Double severityScore;
        private String classification;
        private Integer area;
        private Centroid centroid;
        private Double meanDeltaE;
        private Double peakDeltaE;
        private MeanHsv meanHsv;
        private Double elongation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoundingBox {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Centroid {
        private Double x;
        private Double y;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeanHsv {
        private Double h;
        private Double s;
        private Double v;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetectionMetrics {
        private Double meanSsim;
        private String warpModel;
        private Boolean warpSuccess;
        private Double warpScore;
        private Double thresholdPotential;
        private Double thresholdFault;
        private Double basePotential;
        private Double baseFault;
        private Double sliderPercent;
        private Double scaleApplied;
        private String thresholdSource;
        private Double ratio;
    }
}
