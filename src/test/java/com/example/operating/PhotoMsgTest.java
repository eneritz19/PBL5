package com.example.operating;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.example.PhotoMsg;

class PhotoMsgTest {

    @Test
    void constructor_withoutTimestamp_setsCurrentTime() {
        long before = System.currentTimeMillis();

        PhotoMsg msg = new PhotoMsg("img1", "D1", PhotoMsg.Urgency.ALTO);

        long after = System.currentTimeMillis();

        assertEquals("img1", msg.imageCode);
        assertEquals("D1", msg.doctorId);
        assertEquals(PhotoMsg.Urgency.ALTO, msg.urgency);

        assertTrue(msg.createdAtMillis >= before, "createdAtMillis should be >= before");
        assertTrue(msg.createdAtMillis <= after, "createdAtMillis should be <= after");
    }

    @Test
    void constructor_withTimestamp_preservesProvidedTime() {
        long ts = 123456789L;

        PhotoMsg msg = new PhotoMsg("imgX", "D9", PhotoMsg.Urgency.BAJO, ts);

        assertEquals("imgX", msg.imageCode);
        assertEquals("D9", msg.doctorId);
        assertEquals(PhotoMsg.Urgency.BAJO, msg.urgency);
        assertEquals(ts, msg.createdAtMillis);
    }

    @Test
    void constructor_rejectsNullImageCode() {
        assertThrows(NullPointerException.class,
                () -> new PhotoMsg(null, "D1", PhotoMsg.Urgency.MEDIO, 1L));
    }

    @Test
    void constructor_rejectsNullDoctorId() {
        assertThrows(NullPointerException.class,
                () -> new PhotoMsg("img1", null, PhotoMsg.Urgency.MEDIO, 1L));
    }

    @Test
    void constructor_rejectsNullUrgency() {
        assertThrows(NullPointerException.class,
                () -> new PhotoMsg("img1", "D1", null, 1L));
    }
}
