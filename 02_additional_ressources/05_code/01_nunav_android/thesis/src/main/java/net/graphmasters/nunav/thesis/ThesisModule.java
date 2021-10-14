package net.graphmasters.nunav.thesis;

import android.os.Handler;

import com.google.gson.Gson;

import net.graphmasters.multiplatform.core.geodesy.Geodesy;
import net.graphmasters.multiplatform.core.time.DateTimeFormatter;
import net.graphmasters.multiplatform.core.time.TimeProvider;
import net.graphmasters.multiplatform.navigation.routing.route.provider.dto.MergingRouteDtoConverter;
import net.graphmasters.multiplatform.navigation.routing.route.provider.dto.RouteDtoConverter;
import net.graphmasters.multiplatform.navigation.routing.events.NavigationEventHandler;
import net.graphmasters.multiplatform.navigation.routing.state.NavigationStateProvider;
import net.graphmasters.nunav.android.base.app.CurrentActivityProvider;
import net.graphmasters.nunav.android.base.infrastructure.growls.GrowlManager;
import net.graphmasters.multiplatform.navigation.location.LocationRepository;
import net.graphmasters.nunav.android.base.app.ContextProvider;
import net.graphmasters.nunav.audio.player.AudioJobPlayer;
import net.graphmasters.nunav.core.NunavConfig;
import net.graphmasters.nunav.map.layer.MapLayer;
import net.graphmasters.nunav.mapbox.location.CurrentLocationMapLayer;
import net.graphmasters.nunav.navigation.info.eta.EtaColorProvider;
import net.graphmasters.nunav.navigation.info.eta.TrafficVolumeAwareEtaColorProvider;
import net.graphmasters.nunav.navigation.routing.route.provider.dto.FeatureAwareRouteDtoConverter;
import net.graphmasters.nunav.location.gps.GpsFixProvider;
import net.graphmasters.nunav.thesis.accuracy.audio.AudioWarningLocationAccuracyHandler;
import net.graphmasters.nunav.thesis.accuracy.growl.LocationAccuracyGrowlHandler;
import net.graphmasters.nunav.thesis.accuracy.layer.handler.AccuracyMapLayerHandler;
import net.graphmasters.nunav.thesis.accuracy.notifier.GpsAccuracyNotifier;
import net.graphmasters.nunav.thesis.accuracy.notifier.SchedulingGpsAccuracyNotifier;
import net.graphmasters.nunav.thesis.features.provider.FeaturesProvider;
import net.graphmasters.nunav.thesis.features.provider.RetrofitFeaturesProvider;
import net.graphmasters.nunav.thesis.features.repository.InitialFeaturesRepository;
import net.graphmasters.nunav.thesis.features.repository.FeaturesRepository;
import net.graphmasters.nunav.thesis.usercount.growl.UserCountGrowlHandler;
import net.graphmasters.nunav.thesis.usercount.provider.RetrofitUserCountProvider;
import net.graphmasters.nunav.thesis.usercount.provider.UserCountProvider;
import net.graphmasters.nunav.thesis.usercount.repository.RoutingAwareUserCountRepository;
import net.graphmasters.nunav.thesis.usercount.repository.UserCountRepository;

import java.util.concurrent.Executor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ThesisModule
{

    @Provides
    @Singleton
    FeaturesRepository providesTestableFeaturesRepository(Executor executor, FeaturesProvider featureProvider)
    {
        return new InitialFeaturesRepository(executor, featureProvider);
    }

    @Singleton
    @Provides
    FeaturesProvider provideTestableFeaturesProvider(NunavConfig nunavConfig,
                                                     OkHttpClient okHttpClient,
                                                     Gson gson,
                                                     ContextProvider contextProvider)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(nunavConfig.getServiceUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return new RetrofitFeaturesProvider(retrofit.create(
                RetrofitFeaturesProvider.RetrofitProvider.class), contextProvider);
    }

    @Provides
    @Singleton
    EtaColorProvider providesEtaColorProvider()
    {
        return new TrafficVolumeAwareEtaColorProvider();
    }

    @Provides
    @Singleton
    GpsAccuracyNotifier provideGpsAccuracyNotifier(
            Handler handler,
            GpsFixProvider gpsFixProvider,
            NavigationStateProvider navigationStateProvider,
            LocationRepository locationRepository)
    {
        SchedulingGpsAccuracyNotifier notifier = new SchedulingGpsAccuracyNotifier(
                handler, gpsFixProvider, navigationStateProvider);
        locationRepository.addLocationUpdateListener(notifier);
        return notifier;
    }

    @Provides
    @Singleton
    RouteDtoConverter provideRouteDtoConverter(FeaturesRepository featuresRepository,
                                               TimeProvider timeProvider,
                                               @Named("RouteDateTimeProvider") DateTimeFormatter dateTimeFormatter,
                                               Geodesy geodesy)
    {
        return new FeatureAwareRouteDtoConverter(
                featuresRepository,
                new MergingRouteDtoConverter(timeProvider, dateTimeFormatter, geodesy));
    }

    @Singleton
    @Provides
    UserCountRepository provideUserCountRepository(Executor executor,
                                                   UserCountProvider userCountProvider,
                                                   FeaturesRepository featuresRepository,
                                                   NavigationEventHandler navigationEventHandler)
    {
        RoutingAwareUserCountRepository repository = new RoutingAwareUserCountRepository(executor, userCountProvider, featuresRepository);
        navigationEventHandler.addOnNavigationStoppedListener(repository);
        featuresRepository.addFeaturesLoadedListener(repository);
        return repository;
    }

    @Singleton
    @Provides
    UserCountProvider provideUserCountProvider(NunavConfig nunavConfig,
                                               OkHttpClient okHttpClient,
                                               Gson gson)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(nunavConfig.getServiceUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return new RetrofitUserCountProvider(retrofit.create(
                RetrofitUserCountProvider.RetrofitProvider.class));
    }

    @Singleton
    @Provides
    UserCountGrowlHandler provideUserCountGrowl(GrowlManager growlManager,
                                                NavigationEventHandler navigationEventHandler,
                                                UserCountRepository userCountRepository,
                                                CurrentActivityProvider currentActivityProvider,
                                                FeaturesRepository featuresRepository,
                                                NavigationStateProvider navigationStateProvider)
    {
        UserCountGrowlHandler handler = new UserCountGrowlHandler(
                growlManager, currentActivityProvider, userCountRepository, featuresRepository, navigationStateProvider);
        navigationEventHandler.addOnNavigationStoppedListener(handler);
        navigationEventHandler.addOnNavigationStartedListener(handler);
        userCountRepository.addOnUserCountChangedListener(handler);
        featuresRepository.addFeaturesLoadedListener(handler);
        return handler;
    }

    @Singleton
    @Provides
    GpsAccuracyNotifier.GpsAccuracyListener provideLocationAccuracyGrowlHandler(
            GrowlManager growlManager,
            FeaturesRepository featuresRepository,
            GpsAccuracyNotifier gpsAccuracyNotifier,
            NavigationEventHandler navigationEventHandler,
            ContextProvider contextProvider
    )
    {
        LocationAccuracyGrowlHandler handler = new LocationAccuracyGrowlHandler(
                growlManager, featuresRepository, gpsAccuracyNotifier, contextProvider);
        gpsAccuracyNotifier.addGpsAccuracyListener(handler);
        navigationEventHandler.addOnNavigationStartedListener(handler);
        navigationEventHandler.addOnNavigationStoppedListener(handler);
        return handler;
    }

    @Provides
    @Singleton
    AudioWarningLocationAccuracyHandler provideAudioWarningGpsAccuracyHandler(
            ContextProvider contextProvider,
            AudioJobPlayer audioJobPlayer,
            GpsAccuracyNotifier gpsAccuracyNotifier,
            FeaturesRepository featuresRepository
    )
    {
        AudioWarningLocationAccuracyHandler handler = new AudioWarningLocationAccuracyHandler(
                contextProvider, audioJobPlayer, featuresRepository);
        gpsAccuracyNotifier.addGpsAccuracyListener(handler);
        return handler;
    }

    @Provides
    @Singleton
    AccuracyMapLayerHandler provideAccuracyMapLayerHandler(
            @Named("CurrentLocationMapLayer") MapLayer currentLocationMapLayer,
            ContextProvider contextProvider,
            GpsAccuracyNotifier gpsAccuracyNotifier)
    {
        AccuracyMapLayerHandler handler = new AccuracyMapLayerHandler(currentLocationMapLayer, contextProvider);
        gpsAccuracyNotifier.addGpsAccuracyListener(handler);
        return handler;
    }
}
