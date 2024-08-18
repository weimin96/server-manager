package io.github.weimin96.manager.server.domain.entity;

import io.github.weimin96.manager.server.domain.events.*;
import io.github.weimin96.manager.server.domain.value.*;
import io.github.weimin96.manager.server.services.Registration;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

import static java.util.Collections.*;

/**
 * 实例对象
 *
 * @author panwm
 * @since 2024/8/3 10:41
 */
@Data
@lombok.EqualsAndHashCode(exclude = {"unsavedEvents", "statusTimestamp"})
@lombok.ToString(exclude = "unsavedEvents")
public class Instance implements Serializable {

    /**
     * 实例ID
     */
    private final InstanceId id;

    private final long version;

    /**
     * 注册信息
     */
    private final Registration registration;

    /**
     * 是否注册
     */
    private final boolean registered;

    /**
     * 实例状态信息
     */
    private final StatusInfo statusInfo;

    private final Instant statusTimestamp;

    /**
     * actuator endpoint信息
     */
    private final Info info;

    private final List<InstanceEvent> unsavedEvents;

    private final Endpoints endpoints;

    private final BuildVersion buildVersion;

    private final Tags tags;

    private Instance(InstanceId id) {
        this(id, -1L, null, false, StatusInfo.ofUnknown(), Instant.EPOCH, Info.empty(), Endpoints.empty(), null,
                Tags.empty(), emptyList());
    }

    private Instance(InstanceId id, long version, Registration registration, boolean registered,
                     StatusInfo statusInfo, Instant statusTimestamp, Info info, Endpoints endpoints,
                     BuildVersion buildVersion, Tags tags, List<InstanceEvent> unsavedEvents) {
        Assert.notNull(id, "'id' must not be null");
        Assert.notNull(endpoints, "'endpoints' must not be null");
        Assert.notNull(info, "'info' must not be null");
        Assert.notNull(statusInfo, "'statusInfo' must not be null");
        this.id = id;
        this.version = version;
        this.registration = registration;
        this.registered = registered;
        this.statusInfo = statusInfo;
        this.statusTimestamp = statusTimestamp;
        this.info = info;
        this.endpoints = (registered && (registration != null))
                ? endpoints.withEndpoint(Endpoint.HEALTH, registration.getHealthUrl()) : endpoints;
        this.unsavedEvents = unsavedEvents;
        this.buildVersion = buildVersion;
        this.tags = tags;
    }

    public static Instance create(InstanceId id) {
        Assert.notNull(id, "'id' must not be null");
        return new Instance(id);
    }

    public Instance register(Registration registration) {
        Assert.notNull(registration, "'registration' must not be null");
        // 注册
        if (!this.isRegistered()) {
            return this.apply(new InstanceRegisteredEvent(this.id, this.nextVersion(), registration), true);
        }
        // 更新注册信息
        if (!Objects.equals(this.registration, registration)) {
            return this.apply(new InstanceRegistrationUpdatedEvent(this.id, this.nextVersion(), registration), true);
        }
        return this;
    }

    /**
     * 注销
     *
     * @return Instance
     */
    public Instance deregister() {
        if (this.isRegistered()) {
            return this.apply(new InstanceDeregisteredEvent(this.id, this.nextVersion()), true);
        }
        return this;
    }

    public Instance withInfo(Info info) {
        Assert.notNull(info, "'info' must not be null");
        if (Objects.equals(this.info, info)) {
            return this;
        }
        return this.apply(new InstanceInfoChangedEvent(this.id, this.nextVersion(), info), true);
    }

    public Instance withStatusInfo(StatusInfo statusInfo) {
        Assert.notNull(statusInfo, "'statusInfo' must not be null");
        if (Objects.equals(this.statusInfo.getStatus(), statusInfo.getStatus())) {
            return this;
        }
        return this.apply(new InstanceStatusChangedEvent(this.id, this.nextVersion(), statusInfo), true);
    }

    public Instance withEndpoints(Endpoints endpoints) {
        Assert.notNull(endpoints, "'endpoints' must not be null");
        Endpoints endpointsWithHealth = (this.registration != null)
                ? endpoints.withEndpoint(Endpoint.HEALTH, this.registration.getHealthUrl()) : endpoints;
        if (Objects.equals(this.endpoints, endpointsWithHealth)) {
            return this;
        }
        return this.apply(new InstanceEndpointsDetectedEvent(this.id, this.nextVersion(), endpoints), true);
    }

    public Registration getRegistration() {
        if (this.registration == null) {
            throw new IllegalStateException("Application '" + this.id + "' has no valid registration.");
        }
        return this.registration;
    }

    List<InstanceEvent> getUnsavedEvents() {
        return unmodifiableList(this.unsavedEvents);
    }

    Instance clearUnsavedEvents() {
        return new Instance(this.id, this.version, this.registration, this.registered, this.statusInfo,
                this.statusTimestamp, this.info, this.endpoints, this.buildVersion, this.tags, emptyList());
    }

    Instance apply(Collection<InstanceEvent> events) {
        Assert.notNull(events, "'events' must not be null");
        Instance instance = this;
        for (InstanceEvent event : events) {
            instance = instance.apply(event);
        }
        return instance;
    }

    Instance apply(InstanceEvent event) {
        return this.apply(event, false);
    }

    private Instance apply(InstanceEvent event, boolean isNewEvent) {

        List<InstanceEvent> unsavedEvents = appendToEvents(event, isNewEvent);
        // 注册事件
        if (event instanceof InstanceRegisteredEvent) {
            Registration registration = ((InstanceRegisteredEvent) event).getRegistration();
            return new Instance(this.id, event.getVersion(), registration, true, StatusInfo.ofUnknown(),
                    event.getTimestamp(), Info.empty(), Endpoints.empty(),
                    // 通过注册元数据获取版本和标签
                    updateBuildVersion(registration.getMetadata()), updateTags(registration.getMetadata()),
                    unsavedEvents);

        } else if (event instanceof InstanceRegistrationUpdatedEvent) {
            Registration registration = ((InstanceRegistrationUpdatedEvent) event).getRegistration();
            return new Instance(this.id, event.getVersion(), registration, this.registered, this.statusInfo,
                    this.statusTimestamp, this.info, this.endpoints,
                    updateBuildVersion(registration.getMetadata(), this.info.getValues()),
                    updateTags(registration.getMetadata(), this.info.getValues()), unsavedEvents);

        } else if (event instanceof InstanceStatusChangedEvent) {
            StatusInfo statusInfo = ((InstanceStatusChangedEvent) event).getStatusInfo();
            return new Instance(this.id, event.getVersion(), this.registration, this.registered, statusInfo,
                    event.getTimestamp(), this.info, this.endpoints, this.buildVersion, this.tags, unsavedEvents);

        } else if (event instanceof InstanceEndpointsDetectedEvent) {
            Endpoints endpoints = ((InstanceEndpointsDetectedEvent) event).getEndpoints();
            return new Instance(this.id, event.getVersion(), this.registration, this.registered, this.statusInfo,
                    this.statusTimestamp, this.info, endpoints, this.buildVersion, this.tags, unsavedEvents);

        } else if (event instanceof InstanceInfoChangedEvent) {
            Info info = ((InstanceInfoChangedEvent) event).getInfo();
            Map<String, ?> metaData = (this.registration != null) ? this.registration.getMetadata() : emptyMap();
            return new Instance(this.id, event.getVersion(), this.registration, this.registered, this.statusInfo,
                    this.statusTimestamp, info, this.endpoints, updateBuildVersion(metaData, info.getValues()),
                    updateTags(metaData, info.getValues()), unsavedEvents);

        } else if (event instanceof InstanceDeregisteredEvent) {
            return new Instance(this.id, event.getVersion(), this.registration, false, StatusInfo.ofUnknown(),
                    event.getTimestamp(), Info.empty(), Endpoints.empty(), null, Tags.empty(), unsavedEvents);
        }

        return this;
    }

    private List<InstanceEvent> appendToEvents(InstanceEvent event, boolean isNewEvent) {
        // 旧事件不处理
        if (!isNewEvent) {
            return this.unsavedEvents;
        }
        ArrayList<InstanceEvent> events = new ArrayList<>(this.unsavedEvents.size() + 1);
        events.addAll(this.unsavedEvents);
        events.add(event);
        return events;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    private long nextVersion() {
        return this.version + 1L;
    }

    @SafeVarargs
    private final BuildVersion updateBuildVersion(Map<String, ?>... sources) {
        return Arrays.stream(sources).map(BuildVersion::from).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @SafeVarargs
    private final Tags updateTags(Map<String, ?>... sources) {
        return Arrays.stream(sources).map((source) -> Tags.from(source, "tags")).reduce(Tags.empty(), Tags::append);
    }
}
