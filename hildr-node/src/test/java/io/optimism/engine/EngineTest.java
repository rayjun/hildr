/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.optimism.common.Epoch;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The type EngineTest.
 *
 * @author zhouxing
 * @since 0.1.0
 */
public class EngineTest {

  public static final String AUTH_ADDR = "127.0.0.1";
  public static final String SECRET =
      "f79ae8046bc11c9927afe911db7143c51a806c4a537cc08e0d37140b0192f430";

  public static MockWebServer server;

  @BeforeAll
  static void setUp() throws IOException {
    server = new MockWebServer();
    server.start(8851);
  }

  @AfterAll
  static void tearDown() throws IOException {
    server.shutdown();
  }

  String initForkChoiceUpdateResp() throws JsonProcessingException {
    ForkChoiceUpdate forkChoiceUpdate = new ForkChoiceUpdate();
    forkChoiceUpdate.setPayloadId(new BigInteger("1"));
    PayloadStatus payloadStatus = new PayloadStatus();
    payloadStatus.setStatus(Status.Accepted);
    payloadStatus.setLatestValidHash("asdfadfsdfadsfasdf");
    payloadStatus.setValidationError("");
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(forkChoiceUpdate);
  }

  String initPayloadStatusResp() throws JsonProcessingException {
    PayloadStatus payloadStatus = new PayloadStatus();
    payloadStatus.setStatus(Status.Accepted);
    payloadStatus.setLatestValidHash("12312321");
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(payloadStatus);
  }

  ExecutionPayload initExecutionPayload() {
    ExecutionPayload executionPayload = new ExecutionPayload();
    executionPayload.setBlockHash("sdfasdf12312312");
    executionPayload.setBlockNumber(new BigInteger("1234"));
    executionPayload.setParentHash("sdvkem39441fd132131");
    return executionPayload;
  }

  String initExecutionPayloadJson() throws JsonProcessingException {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(initExecutionPayload());
  }

  @Test
  void testForkChoiceUpdate()
      throws JsonProcessingException, InterruptedException, ExecutionException {
    String baseUrl = EngineApi.authUrlFromAddr(AUTH_ADDR, null);
    assertEquals("http://127.0.0.1:8851", baseUrl);
    server.enqueue(new MockResponse().setBody(initForkChoiceUpdateResp()));
    EngineApi engineApi = new EngineApi(baseUrl, SECRET);
    ForkchoiceState forkchoiceState = new ForkchoiceState("123", "123", "!@3");
    PayloadAttributes payloadAttributes =
        new PayloadAttributes(
            new BigInteger("123123"),
            "123123",
            "123",
            List.of(""),
            false,
            new BigInteger("1"),
            new Epoch(new BigInteger("12"), "123", new BigInteger("1233145")),
            new BigInteger("1334"),
            new BigInteger("321"));
    CompletableFuture<ForkChoiceUpdate> future =
        engineApi.forkChoiceUpdate(forkchoiceState, payloadAttributes);
    ForkChoiceUpdate forkChoiceUpdate = future.get();
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    assertEquals(initForkChoiceUpdateResp(), ow.writeValueAsString(forkChoiceUpdate));
  }

  @Test
  void testNewPayload() throws JsonProcessingException, ExecutionException, InterruptedException {
    String baseUrl = EngineApi.authUrlFromAddr(AUTH_ADDR, null);
    assertEquals("http://127.0.0.1:8851", baseUrl);
    server.enqueue(new MockResponse().setBody(initPayloadStatusResp()));
    EngineApi engineApi = new EngineApi(baseUrl, SECRET);
    CompletableFuture<PayloadStatus> future = engineApi.newPayload(initExecutionPayload());
    PayloadStatus payloadStatus = future.get();
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    assertEquals(initPayloadStatusResp(), ow.writeValueAsString(payloadStatus));
  }

  @Test
  void testGetPayload() throws JsonProcessingException, ExecutionException, InterruptedException {
    String baseUrl = EngineApi.authUrlFromAddr(AUTH_ADDR, null);
    assertEquals("http://127.0.0.1:8851", baseUrl);
    server.enqueue(new MockResponse().setBody(initExecutionPayloadJson()));
    EngineApi engineApi = new EngineApi(baseUrl, SECRET);
    CompletableFuture<ExecutionPayload> future = engineApi.getPayload(new BigInteger("123"));
    ExecutionPayload executionPayload = future.get();
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    assertEquals(initExecutionPayloadJson(), ow.writeValueAsString(executionPayload));
  }
}