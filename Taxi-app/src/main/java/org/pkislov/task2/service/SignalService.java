package org.pkislov.task2.service;import lombok.RequiredArgsConstructor;import lombok.extern.slf4j.Slf4j;import org.pkislov.task2.dto.DistanceDto;import org.pkislov.task2.dto.SignalDto;import org.springframework.beans.factory.annotation.Value;import org.springframework.kafka.core.KafkaTemplate;import org.springframework.stereotype.Service;import java.util.List;import java.util.Map;import java.util.Optional;import java.util.concurrent.ConcurrentHashMap;@Slf4j@Service@RequiredArgsConstructorpublic class SignalService {    private final KafkaTemplate<String, SignalDto> kafkaSignalDtoTemplate;    private final KafkaTemplate<String, DistanceDto> kafkaDistanceDtoTemplate;    private final Map<Long, SignalDto> signals = new ConcurrentHashMap<>();    @Value("${spring.kafka.topic.input}")    private String inputTopic;    @Value("${spring.kafka.topic.output}")    private String outputTopic;    public void sendSignalToInputQueue(List<SignalDto> signalDtos) {        for (SignalDto signalDto : signalDtos) {            kafkaSignalDtoTemplate.send(inputTopic, signalDto);        }    }    public void processDistance(SignalDto signalDto) {        var distanceResult = calculateDifferenceBetweenSignals(signalDto);        if (distanceResult.isEmpty()) {            log.info("There is no previous signal");            return;        }        log.info("Difference between signals is {}", distanceResult.get());        var distanceDto = new DistanceDto(signalDto.getId(), distanceResult.get());        kafkaDistanceDtoTemplate.send(outputTopic, distanceDto);        log.info("DistanceDto %s sent to %s topic".formatted(distanceDto, outputTopic));    }    private Optional<Double> calculateDifferenceBetweenSignals(SignalDto newSignal) {        if (!signals.containsKey(newSignal.getId())) {            signals.put(newSignal.getId(), newSignal);            return Optional.empty();        }        var oldSignal = signals.get(newSignal.getId());        var distance = calculateDifferenceBetweenSignals(newSignal, oldSignal);        signals.put(newSignal.getId(), newSignal);        return Optional.of(distance);    }    private double calculateDifferenceBetweenSignals(SignalDto newSignal, SignalDto oldSignal) {        return calculateDistance(newSignal.getX(), newSignal.getY(), oldSignal.getX(), oldSignal.getY());    }    private double calculateDistance(double x1, double y1, double x2, double y2) {        double deltaX = x2 - x1;        double deltaY = y2 - y1;        double deltaX2 = deltaX * deltaX;        double deltaY2 = deltaY * deltaY;        return Math.sqrt(deltaX2 + deltaY2);    }}