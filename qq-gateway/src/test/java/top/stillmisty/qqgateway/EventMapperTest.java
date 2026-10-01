package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class EventMapperTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void mapsGroupAtMessageWithMemberOpenid() {
    QqIncomingMessage message =
        map(
            """
            {"op":0,"s":1,"t":"GROUP_AT_MESSAGE_CREATE","id":"EVENT-1","d":{
              "id":"MSG-1","content":"状态","timestamp":"2026-07-21T08:00:00+08:00",
              "group_openid":"GROUP-1",
              "author":{"id":"AUTHOR-1","member_openid":"MEMBER-1","username":"小明","bot":false}}}
            """);

    assertEquals("EVENT-1", message.eventId());
    assertEquals("MSG-1", message.messageId());
    assertEquals(QqScene.GROUP_AT, message.scene());
    assertEquals("MEMBER-1", message.openId());
    assertEquals("GROUP-1", message.groupOpenId());
    assertEquals("状态", message.content());
    assertEquals("小明", message.authorName());
    assertEquals(Instant.parse("2026-07-21T00:00:00Z"), message.timestamp());
  }

  @Test
  void mapsC2cMessageWithUserOpenidAndNullGroup() {
    QqIncomingMessage message =
        map(
            """
            {"op":0,"s":2,"t":"C2C_MESSAGE_CREATE","id":"EVENT-2","d":{
              "id":"MSG-2","content":"hi",
              "author":{"id":"AUTHOR-2","user_openid":"USER-2","username":"小红"}}}
            """);

    assertEquals(QqScene.C2C, message.scene());
    assertEquals("USER-2", message.openId());
    assertNull(message.groupOpenId());
    assertEquals(Instant.EPOCH, message.timestamp());
  }

  @Test
  void groupMessageSceneIsMapped() {
    QqIncomingMessage message =
        map(
            """
            {"op":0,"s":3,"t":"GROUP_MESSAGE_CREATE","id":"EVENT-3","d":{
              "id":"MSG-3","content":"hello","group_openid":"GROUP-9",
              "author":{"member_openid":"MEMBER-9"}}}
            """);

    assertEquals(QqScene.GROUP_MESSAGE, message.scene());
    assertEquals("MEMBER-9", message.openId());
    assertEquals("GROUP-9", message.groupOpenId());
  }

  @Test
  void unknownTypeReturnsNull() {
    assertNull(
        EventMapper.map(
            MAPPER.readTree("{\"op\":0,\"s\":9,\"t\":\"GROUP_ADD_ROBOT\",\"id\":\"E\",\"d\":{}}")));
  }

  @Test
  void fallsBackToAuthorIdWhenSpecificOpenidMissing() {
    QqIncomingMessage message =
        map(
            """
            {"op":0,"s":4,"t":"GROUP_AT_MESSAGE_CREATE","id":"EVENT-4","d":{
              "id":"MSG-4","content":"x","group_openid":"GROUP-1",
              "author":{"id":"AUTHOR-4"}}}
            """);

    assertEquals("AUTHOR-4", message.openId());
  }

  @Test
  void missingMessageIdReturnsNull() {
    assertNull(
        EventMapper.map(
            MAPPER.readTree(
                """
                {"op":0,"s":5,"t":"GROUP_AT_MESSAGE_CREATE","id":"EVENT-5","d":{
                  "content":"x","group_openid":"GROUP-1",
                  "author":{"member_openid":"MEMBER-5"}}}
                """)));
  }

  @Test
  void malformedTimestampFallsBackToEpoch() {
    QqIncomingMessage message =
        map(
            """
            {"op":0,"s":6,"t":"GROUP_AT_MESSAGE_CREATE","id":"EVENT-6","d":{
              "id":"MSG-6","content":"x","timestamp":"not-a-time","group_openid":"GROUP-1",
              "author":{"member_openid":"MEMBER-6"}}}
            """);

    assertEquals(Instant.EPOCH, message.timestamp());
  }

  @Test
  void timestampOfPrefersIsoTimestamp() {
    assertEquals(
        Instant.parse("2026-07-21T00:00:00Z"),
        EventMapper.timestampOf(
            MAPPER.readTree(
                "{\"d\":{\"timestamp\":\"2026-07-21T08:00:00+08:00\",\"event_ts\":\"1\"}}")));
  }

  @Test
  void timestampOfFallsBackToEventTs() {
    Instant expected = Instant.ofEpochSecond(1725442341L);
    assertEquals(
        expected,
        EventMapper.timestampOf(MAPPER.readTree("{\"d\":{\"event_ts\":\"1725442341\"}}")));
    assertEquals(
        expected, EventMapper.timestampOf(MAPPER.readTree("{\"d\":{\"event_ts\":1725442341}}")));
  }

  @Test
  void timestampOfReturnsNullWhenAbsentOrUnparsable() {
    assertNull(EventMapper.timestampOf(MAPPER.readTree("{\"d\":{}}")));
    assertNull(EventMapper.timestampOf(MAPPER.readTree("{\"d\":{\"timestamp\":\"not-a-time\"}}")));
    assertNull(EventMapper.timestampOf(MAPPER.readTree("{\"d\":{\"event_ts\":\"abc\"}}")));
  }

  private static QqIncomingMessage map(String json) {
    return Objects.requireNonNull(EventMapper.map(MAPPER.readTree(json)));
  }
}
