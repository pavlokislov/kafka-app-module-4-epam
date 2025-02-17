package org.pkislov.task2.listener;import lombok.RequiredArgsConstructor;import lombok.extern.slf4j.Slf4j;import org.pkislov.task2.dto.SignalDto;import org.pkislov.task2.service.SignalService;import org.springframework.beans.factory.annotation.Value;import org.springframework.kafka.annotation.KafkaListener;import org.springframework.kafka.annotation.TopicPartition;import org.springframework.stereotype.Component;@Slf4j@Component@RequiredArgsConstructorpublic class InputListeners {    @Value("${spring.kafka.topic.input}")    private String inputTopic;    private final SignalService signalService;    @KafkaListener(            topicPartitions = @TopicPartition(                    topic = "${spring.kafka.topic.input}",                    partitions = {"0,1,2"}            ),            groupId = "${spring.kafka.consumer.group-id}",            containerFactory = "kafkaListenerContainerFactory")    public void trackerConsumer(SignalDto signalDto) {        log.info("Received Message: %s in topic %s".formatted(signalDto, inputTopic));        signalService.processDistance(signalDto);    }}