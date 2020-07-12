//package com.christinewang;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import com.despegar.http.client.GetMethod;
//import com.despegar.http.client.HttpClientException;
//import com.despegar.http.client.HttpResponse;
//import com.despegar.sparkjava.test.SparkServer;
//import org.junit.ClassRule;
//import org.junit.Test;
//import spark.servlet.SparkApplication;
//
///**
// * Unit test for simple App.
// */
//public class ControllerTest {
//    private static final int HTTP_OK = 200;
//    private static final int HTTP_BAD_REQUEST = 404;
//
//    public static class ControllerTestApp implements SparkApplication {
//        public void init() {
//            String [] args = {};
//            Controller.main(args);
//        }
//    }
//
//    @ClassRule
//    public static SparkServer<ControllerTestApp> testServer = new SparkServer<>(ControllerTestApp.class, 4567);
//
//    @Test
//    public void testHomePath() throws HttpClientException {
//        GetMethod request = testServer.get("/", false);
//        HttpResponse httpResponse = testServer.execute(request);
//        assertEquals(HTTP_OK, httpResponse.code());
//    }
//
//    @Test
//    public void testInvalidPath() throws HttpClientException {
//        GetMethod request = testServer.get("/blah", false);
//        HttpResponse httpResponse = testServer.execute(request);
//        assertEquals(HTTP_BAD_REQUEST, httpResponse.code());
//    }
//
//    /**
//     * Test invalid precinct numbers.
//     * @param path either the strings 'start_vote', 'end_vote', or 'wait_time'
//     * @throws HttpClientException
//     */
//    public void testInvalidPrecinctImpl(String path) throws HttpClientException {
//        // invalid path - string as precint
//        GetMethod request = testServer.get(String.format("/%s/blah", path), false);
//        HttpResponse httpResponse = testServer.execute(request);
//        assertEquals(HTTP_BAD_REQUEST, httpResponse.code());
//
//        // invalid path - empty precinct
//        request = testServer.get(String.format("/%s/", path), false);
//        httpResponse = testServer.execute(request);
//        assertEquals(HTTP_BAD_REQUEST, httpResponse.code());
//    }
//
//    @Test
//    public void testInvalidPrecinct() throws HttpClientException {
//        testInvalidPrecinctImpl("start_vote");
//        testInvalidPrecinctImpl("end_vote");
//        testInvalidPrecinctImpl("wait_time");
//    }
//
//    /**
//     * Tests a valid registered vote. Starts then ends the vote.
//     * @throws HttpClientException
//     */
//    @Test
//    public void testValidVote() throws HttpClientException {
//        GetMethod request = testServer.get("/start_vote/1", false);
//        HttpResponse httpResponse = testServer.execute(request);
//        assertEquals(HTTP_OK, httpResponse.code());
//        // TODO - why is cookie management disabled for this library?
////        request = testServer.get("/end_vote/1", false);
////        httpResponse = testServer.execute(request);
////        assertEquals(HTTP_OK, httpResponse.code());
//    }
//}
