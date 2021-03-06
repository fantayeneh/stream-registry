/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import static com.expediagroup.streamplatform.streamregistry.core.events.ObjectNodeMapper.deserialise;
import static java.lang.String.valueOf;

import java.util.Collections;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.avro.AvroEvent;
import com.expediagroup.streamplatform.streamregistry.avro.AvroKey;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;

@Slf4j
public class ProducerNotificationEventUtilsTest {

  @Test
  public void having_a_complete_producer_verify_that_is_correctly_built() {
    Function<Producer, AvroKey> toKeyRecord = ProducerNotificationEventUtils::toAvroKeyRecord;

    Function<Producer, AvroEvent> toValueRecord = ProducerNotificationEventUtils::toAvroValueRecord;

    val producerName = "producer1";
    val streamName = "name";
    val domain = "domain";
    val description = "description";
    val type = "type";
    val configJson = "{}";
    val statusJson = "{\"foo\":\"bar\"}";
    val tags = Collections.singletonList(new Tag("tag-name", "tag-value"));
    val version = 1;
    val zone = "aws_us_east_1";

    // Key
    val key = new ProducerKey();
    key.setName(producerName);
    key.setStreamName(streamName);
    key.setStreamDomain(domain);
    key.setStreamVersion(version);
    key.setZone(zone);

    // Spec
    val spec = new Specification();
    spec.setDescription(description);
    spec.setType(type);
    spec.setConfiguration(deserialise(configJson));
    spec.setTags(tags);

    // Status
    Status status = new Status(deserialise(statusJson));

    val producer = new Producer();
    producer.setKey(key);
    producer.setSpecification(spec);
    producer.setStatus(status);

    AvroKey avroKey = toKeyRecord.apply(producer);
    log.info("Obtained avro key {}", avroKey);

    Assert.assertNotNull("Avro key shouldn't be null", avroKey);
    Assert.assertNotNull("Key id shouldn't be null", avroKey.getId());
    Assert.assertEquals("key's is should be the same as producerName", producerName, avroKey.getId());
    Assert.assertEquals("key's parent should be the same as version", valueOf(version), avroKey.getParent().getId());
    Assert.assertEquals("versionKey's parent should be the same as streamName", streamName, avroKey.getParent().getParent().getId());

    AvroEvent avroEvent = toValueRecord.apply(producer);
    log.info("Obtained avro event {}", avroEvent);

    Assert.assertNotNull("Avro event shouldn't be null", avroEvent);
    Assert.assertNotNull("producer entity shouldn't be null", avroEvent.getProducerEntity());
    Assert.assertEquals(producerName, avroEvent.getProducerEntity().getName());
    Assert.assertEquals(streamName, avroEvent.getProducerEntity().getStreamName());
    Assert.assertEquals(domain, avroEvent.getProducerEntity().getStreamDomain());
    Assert.assertEquals(zone, avroEvent.getProducerEntity().getZone());
    Assert.assertEquals(description, avroEvent.getProducerEntity().getDescription());
    Assert.assertEquals(type, avroEvent.getProducerEntity().getType());
    Assert.assertEquals(configJson, avroEvent.getProducerEntity().getConfigurationString());
    Assert.assertEquals(statusJson, avroEvent.getProducerEntity().getStatusString());
    Assert.assertEquals(version, avroEvent.getProducerEntity().getStreamVersion().intValue());
  }

}
