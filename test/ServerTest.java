import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Account;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

import models.Transfer;
import org.junit.Test;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithServer;
import java.io.*;
import java.util.concurrent.CompletionStage;


public class ServerTest extends WithServer {

    @Test
    public void testInServer() throws Exception {
        String url = "http://localhost:" + this.testServer.port() + "/";
        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(NOT_FOUND, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBadRoute() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/xx/Kiwi");

        Result result = route(app, request);
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void applicationTest() {
        // Section 1: invalid account creation
        {
            Account account = new Account();
            Http.RequestBuilder request = Helpers
                    .fakeRequest(POST, "/accounts")
                    .bodyJson(Json.toJson(account));
            Result result = route(app, request);
            assertEquals(BAD_REQUEST, result.status());

            account.setName("account");
            request.bodyJson(Json.toJson(account));
            result = route(app, request);
            assertEquals(BAD_REQUEST, result.status());

            account.setName("");
            account.setOwnerName("me");
            request.bodyJson(Json.toJson(account));
            result = route(app, request);
            assertEquals(BAD_REQUEST, result.status());

            account.setOwnerName("");
            account.setBalance(10f);
            request.bodyJson(Json.toJson(account));
            result = route(app, request);
            assertEquals(BAD_REQUEST, result.status());
        }

        // Section 2: validate account create, read, update and delete
        {
            Account account1 = new Account("savings", "vasco", 200f);
            Account account2 = new Account("checking", "john", 50f);
            Account account3 = new Account("savings", "john", 5000f);
            Account account4 = new Account("testAccount", "no one", 1f);

            // create
            Http.RequestBuilder request = Helpers
                    .fakeRequest(POST, "/accounts")
                    .bodyJson(Json.toJson(account1));
            Result result = route(app, request);
            assertEquals(CREATED, result.status());
            assertAccount(getJsonNodeFromResult(result), "savings", "vasco", 200f);

            request.bodyJson(Json.toJson(account2));
            result = route(app, request);
            assertEquals(CREATED, result.status());
            assertAccount(getJsonNodeFromResult(result), "checking", "john", 50f);

            request.bodyJson(Json.toJson(account3));
            result = route(app, request);
            assertEquals(CREATED, result.status());
            assertAccount(getJsonNodeFromResult(result), "savings", "john", 5000f);

            request.bodyJson(Json.toJson(account4));
            result = route(app, request);
            assertEquals(CREATED, result.status());
            account4 = Json.fromJson(getJsonNodeFromResult(result), Account.class);
            assertAccount(getJsonNodeFromResult(result), "testAccount", "no one", 1f);

            // read invalid id
            request = Helpers.fakeRequest(GET, "/accounts/204382");
            result = route(app, request);
            assertEquals(NOT_FOUND, result.status());
            // read valid id
            request = Helpers.fakeRequest(GET, "/accounts/" + account4.getId());
            result = route(app, request);
            assertEquals(OK, result.status());
            assertAccount(getJsonNodeFromResult(result), "testAccount", "no one", 1f);

            // invalid id
            account4.setOwnerName("vasco");
            request = Helpers.fakeRequest(PUT, "/accounts/456").bodyJson(Json.toJson(account4));
            result = route(app, request);
            assertEquals(FORBIDDEN, result.status());
            // now with valid resource id
            request = Helpers.fakeRequest(PUT, "/accounts/" + account4.getId()).bodyJson(Json.toJson(account4));
            result = route(app, request);
            assertEquals(OK, result.status());
            Account placeholder = Json.fromJson(getJsonNodeFromResult(result), Account.class);
            assertEquals(account4.getId(), placeholder.getId());
            assertEquals("vasco", placeholder.getOwnerName());
            // try to delete fake id
            request = Helpers.fakeRequest(DELETE, "/accounts/5555");
            result = route(app, request);
            assertEquals(NOT_FOUND, result.status());
            // now delete it
            request = Helpers.fakeRequest(DELETE, "/accounts/" + account4.getId());
            result = route(app, request);
            assertEquals(NO_CONTENT, result.status());
        }

        // Section 3: filtering and sorting accounts
        {
            // filter by name 'savings' -> should get account 1 and 3
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/accounts?name=savings");
            Result result = route(app, request);
            JsonNode list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "vasco", 200f);
            assertAccount(list.get(1), "savings", "john", 5000f);
            // filter by owner name 'john' -> should get account 2 and 3
            request = Helpers.fakeRequest(GET, "/accounts?ownerName=john");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "checking", "john", 50f);
            assertAccount(list.get(1), "savings", "john", 5000f);
            // filter by exact balance
            request = Helpers.fakeRequest(GET, "/accounts?balance=50");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "checking", "john", 50f);
            // filter above balance
            request = Helpers.fakeRequest(GET, "/accounts?aboveBalance=100");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "vasco", 200f);
            assertAccount(list.get(1), "savings", "john", 5000f);
            // filter below balance
            request = Helpers.fakeRequest(GET, "/accounts?belowBalance=1000");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "vasco", 200f);
            assertAccount(list.get(1), "checking", "john", 50f);
            // filter between balances
            request = Helpers.fakeRequest(GET, "/accounts?aboveBalance=200&belowBalance=10000");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "john", 5000f);
            // sort by name
            request = Helpers.fakeRequest(GET, "/accounts?sort=name");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "checking", "john", 50f);
            assertAccount(list.get(1), "savings", "vasco", 200f);
            assertAccount(list.get(2), "savings", "john", 5000f);
            // sort by name desc
            request = Helpers.fakeRequest(GET, "/accounts?sort=-name");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "vasco", 200f);
            assertAccount(list.get(1), "savings", "john", 5000f);
            assertAccount(list.get(2), "checking", "john", 50f);
            // sort by ownerName
            request = Helpers.fakeRequest(GET, "/accounts?sort=ownerName");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "checking", "john", 50f);
            assertAccount(list.get(1), "savings", "john", 5000f);
            assertAccount(list.get(2), "savings", "vasco", 200f);
            // sort by ownerName desc
            request = Helpers.fakeRequest(GET, "/accounts?sort=-ownerName");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "vasco", 200f);
            assertAccount(list.get(1), "checking", "john", 50f);
            assertAccount(list.get(2), "savings", "john", 5000f);
            // sort by balance
            request = Helpers.fakeRequest(GET, "/accounts?sort=balance");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "checking", "john", 50f);
            assertAccount(list.get(1), "savings", "vasco", 200f);
            assertAccount(list.get(2), "savings", "john", 5000f);
            // sort by balance desc
            request = Helpers.fakeRequest(GET, "/accounts?sort=-balance");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertAccount(list.get(0), "savings", "john", 5000f);
            assertAccount(list.get(1), "savings", "vasco", 200f);
            assertAccount(list.get(2), "checking", "john", 50f);
        }
        // Section 4: Deposits and withdrawals
        {
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/accounts");
            Result result = route(app, request);
            JsonNode list = getJsonNodeFromResult(result);
            Account account1 = Json.fromJson(list.get(0), Account.class);

            // deposit to invalid id
            request = Helpers.fakeRequest(PUT, "/accounts/092304903/deposit/30");
            result = route(app, request);
            assertEquals(NOT_FOUND, result.status());
            // deposit to valid id
            request = Helpers.fakeRequest(PUT, "/accounts/" + account1.getId() + "/deposit/30");
            result = route(app, request);
            assertEquals(OK, result.status());
            assertAccount(getJsonNodeFromResult(result), "savings", "vasco", 230f);
            // withdraw from invalid id
            request = Helpers.fakeRequest(PUT, "/accounts/alll/withdraw/30");
            result = route(app, request);
            assertEquals(NOT_FOUND, result.status());
            // withdraw from valid id
            request = Helpers.fakeRequest(PUT, "/accounts/" + account1.getId() + "/withdraw/30");
            result = route(app, request);
            assertEquals(OK, result.status());
            assertAccount(getJsonNodeFromResult(result), "savings", "vasco", 200f);
        }
        // Section 5: Invalid transfer creation
        {
            Transfer transfer1 = new Transfer("0", "1", -1);
            Transfer transfer2 = new Transfer("portugal", "1", 100);
            Transfer transfer3 = new Transfer("0", "1", 550);

            Http.RequestBuilder request = Helpers
                    .fakeRequest(POST, "/transfers")
                    .bodyJson(Json.toJson(transfer1));
            Result result = route(app, request);
            assertEquals(FORBIDDEN, result.status());
            request.bodyJson(Json.toJson(transfer2));
            result = route(app, request);
            assertEquals(FORBIDDEN, result.status());
            request.bodyJson(Json.toJson(transfer3));
            result = route(app, request);
            assertEquals(FORBIDDEN, result.status());
        }
        // Section 6: Transfer create, read and delete
        {
            Transfer transfer1 = new Transfer("0", "1", 10f);
            Transfer transfer2 = new Transfer("0", "1", 100f);
            Transfer transfer3 = new Transfer("1", "0", 90f);
            Transfer transfer4 = new Transfer("1", "0", 25f);
            Transfer transfer5 = new Transfer("1", "0", 1000000f);

            Http.RequestBuilder request = Helpers
                    .fakeRequest(POST, "/transfers")
                    .bodyJson(Json.toJson(transfer1));
            Result result = route(app, request);
            assertEquals(CREATED, result.status());
            assertTransfer(getJsonNodeFromResult(result), "0", "1", 10f);

            request.bodyJson(Json.toJson(transfer2));
            result = route(app, request);
            assertEquals(CREATED, result.status());
            assertTransfer(getJsonNodeFromResult(result), "0", "1", 100f);

            request.bodyJson(Json.toJson(transfer3));
            result = route(app, request);
            assertEquals(CREATED, result.status());
            assertTransfer(getJsonNodeFromResult(result), "1", "0", 90f);

            request.bodyJson(Json.toJson(transfer4));
            result = route(app, request);
            assertEquals(CREATED, result.status());
            transfer4 = Json.fromJson(getJsonNodeFromResult(result), Transfer.class);
            assertTransfer(getJsonNodeFromResult(result), "1", "0", 25f);

            // transfer 5 will fail due to insufficient funds
            request.bodyJson(Json.toJson(transfer5));
            result = route(app, request);
            assertEquals(FORBIDDEN, result.status());

            // read invalid transfer id
            request = Helpers.fakeRequest(GET, "/transfers/00");
            result = route(app, request);
            assertEquals(NOT_FOUND, result.status());
            // read valid transfer id
            request = Helpers.fakeRequest(GET, "/transfers/" + transfer4.getId());
            result = route(app, request);
            assertEquals(OK, result.status());
            assertTransfer(getJsonNodeFromResult(result), "1", "0", 25f);

            // delete invalid transfer id
            request = Helpers.fakeRequest(DELETE, "/transfers/00");
            result = route(app, request);
            assertEquals(NOT_FOUND, result.status());
            // delete valid transfer id
            request = Helpers.fakeRequest(DELETE, "/transfers/" + transfer4.getId());
            result = route(app, request);
            assertEquals(NO_CONTENT, result.status());

            // now let's check balance of accounts
            request = Helpers.fakeRequest(GET, "/accounts/0");
            result = route(app, request);
            assertAccount(getJsonNodeFromResult(result), "savings", "vasco", 205f);
            request = Helpers.fakeRequest(GET, "/accounts/1");
            result = route(app, request);
            assertAccount(getJsonNodeFromResult(result), "checking", "john", 45f);
        }
        // Section 7: filtering and sorting transfers
        {
            // filter by origin account id '0' -> should get transfer 1 and 2
            Http.RequestBuilder request = Helpers.fakeRequest(GET, "/transfers?originAccountId=0");
            Result result = route(app, request);
            JsonNode list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 10f);
            assertTransfer(list.get(1), "0", "1", 100f);
            // filter by destination account id '0' -> should get transfer 3
            request = Helpers.fakeRequest(GET, "/transfers?destinationAccountId=0");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "1", "0", 90f);
            // filter above amount
            request = Helpers.fakeRequest(GET, "/transfers?aboveAmount=91");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 100f);
            // filter below amount
            request = Helpers.fakeRequest(GET, "/transfers?belowAmount=90");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 10f);
            // filter between balances
            request = Helpers.fakeRequest(GET, "/transfers?aboveAmount=20&belowAmount=100");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "1", "0", 90f);
            // sort by origin account id
            request = Helpers.fakeRequest(GET, "/transfers?sort=originAccountId");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 10f);
            assertTransfer(list.get(1), "0", "1", 100f);
            assertTransfer(list.get(2), "1", "0", 90f);
            // sort by origin account id desc
            request = Helpers.fakeRequest(GET, "/transfers?sort=-originAccountId");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "1", "0", 90f);
            assertTransfer(list.get(1), "0", "1", 10f);
            assertTransfer(list.get(2), "0", "1", 100f);
            // sort by destination account id
            request = Helpers.fakeRequest(GET, "/transfers?sort=destinationAccountId");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "1", "0", 90f);
            assertTransfer(list.get(1), "0", "1", 10f);
            assertTransfer(list.get(2), "0", "1", 100f);
            // sort by destination account id desc
            request = Helpers.fakeRequest(GET, "/transfers?sort=-destinationAccountId");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 10f);
            assertTransfer(list.get(1), "0", "1", 100f);
            assertTransfer(list.get(2), "1", "0", 90f);
            // sort by amount
            request = Helpers.fakeRequest(GET, "/transfers?sort=amount");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 10f);
            assertTransfer(list.get(1), "1", "0", 90f);
            assertTransfer(list.get(2), "0", "1", 100f);
            // sort by balance desc
            request = Helpers.fakeRequest(GET, "/transfers?sort=-amount");
            result = route(app, request);
            list = getJsonNodeFromResult(result);
            assertTransfer(list.get(0), "0", "1", 100f);
            assertTransfer(list.get(1), "1", "0", 90f);
            assertTransfer(list.get(2), "0", "1", 10f);
        }
    }

    private void assertAccount(JsonNode json, String expectedName, String expectedOwnerName, Float expectedBalance){
        Account account = Json.fromJson(json, Account.class);
        assertEquals(expectedName, account.getName());
        assertEquals(expectedOwnerName, account.getOwnerName());
        assertEquals(expectedBalance, account.getBalance(), 0.01);
    }

    private void assertTransfer(JsonNode json, String expectedOriginAccountId, String expectedDestinationAccountId, Float expectedAmount){
        Transfer transfer = Json.fromJson(json, Transfer.class);
        assertNotNull(transfer.getId());
        assertNotNull(transfer.getTimestamp());
        assertEquals(expectedOriginAccountId, transfer.getOriginAccountId());
        assertEquals(expectedDestinationAccountId, transfer.getDestinationAccountId());
        assertEquals(expectedAmount, transfer.getAmount(), 0.01);
    }

    private JsonNode getJsonNodeFromResult(Result result){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = null;
        try {
            json = mapper.readValue(Helpers.contentAsString(result), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}
