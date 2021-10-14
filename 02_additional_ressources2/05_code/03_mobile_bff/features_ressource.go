package features

import (
	"encoding/json"
	"github.com/Graphmasters/ios-nunav-navigation-bff-go/bff"
	"github.com/gorilla/mux"
	"github.com/prometheus/client_golang/prometheus"
	"hash/fnv"
	"net/http"
	"strings"
	"time"
)

type Response struct {
	Body Body `json:"body"`
}

type Body struct {
	Features  map[string]bool `json:"features,omitempty"`
}

const (
	TrafficVolume    = "traffic-volume"
	GpsQuality       = "gps-quality"
	UserCount        = "user-count"
	RouteExplanation = "route-explanation"

	EnableAllFeatureFlag = "enableAllFeatures"

	TestGroupAllFeatures = 3
	TestGroupNoFeatures = 0
)

type FeatureRessource struct {
	metrics *bff.Metrics
}

func NewUserGroupResource() *FeatureRessource {
	return &FeatureRessource{}
}

func (ms *FeatureRessource) SetMetrics(metrics *bff.Metrics) {
	ms.metrics = metrics
}

func (ms *FeatureRessource) AddCustomMetrics(registerer prometheus.Registerer) error {
	return nil
}

func (ms *FeatureRessource) RegisterHandlers(router *mux.Router) {
	router.HandleFunc("/v1/features/available", ms.handleUserCountRequest)
}

func (ms *FeatureRessource) handleUserCountRequest(writer http.ResponseWriter, request *http.Request) {
	start := time.Now()
	platformInfo := bff.ParsePlatformInfo(request)

	if ms.metrics != nil {
		ms.metrics.RequestCounterVec.WithLabelValues("handleStudyGroup").Inc()
	}

	writer.Header().Add("Content-Type", "application/json")
	response := ms.getFeatureResponse(platformInfo, request)

	err := json.NewEncoder(writer).Encode(response)
	if err != nil {
		bff.WriteError(writer, request, err, err.Error(), http.StatusInternalServerError, "UsersResource", ms.metrics, start)
	}
}

func hash(s string) uint32 {
	h := fnv.New32a()
	h.Write([]byte(s))
	return h.Sum32()
}

func (ms *FeatureRessource) getFeatureResponse(platformInfo bff.PlatformInfo, request *http.Request) Response {
	if request.URL.Query().Get(EnableAllFeatureFlag) == "true" {
		return ms.getFeatureForTestGroup(TestGroupAllFeatures, platformInfo)
	}

	if !((strings.Contains(platformInfo.Locale, "de") ||
		strings.Contains(platformInfo.Locale, "en")) &&
		strings.Contains(platformInfo.ApplicationId, "com.nunav.play")) {
		return Response{Body: Body{Features: map[string]bool{}}}
	}

	return ms.getFeatureForTestGroup(hash(request.Header.Get("Instance-Id")) % 4, platformInfo)
}

func (ms *FeatureRessource) getFeatureForTestGroup(testGroup uint32, platformInfo bff.PlatformInfo) Response {
	switch testGroup {
	case 1:
		return Response{Body: Body{Features: map[string]bool{
			TrafficVolume:    true,
			GpsQuality:       true,
			UserCount:        false,
			RouteExplanation: false,
		}}}
	case 2:
		return Response{Body: Body{Features: map[string]bool{
			TrafficVolume:    false,
			GpsQuality:       false,
			UserCount:        platformInfo.VersionCode > 1528,
			RouteExplanation: true,
		}}}
	case TestGroupAllFeatures:
		return Response{Body: Body{Features: map[string]bool{
			TrafficVolume:    true,
			GpsQuality:       true,
			UserCount:        platformInfo.VersionCode > 1528,
			RouteExplanation: true,
		}}}
	default:
		return Response{Body: Body{Features: map[string]bool{
			TrafficVolume:    false,
			GpsQuality:       false,
			UserCount:        false,
			RouteExplanation: false,
		}}}
	}
}
