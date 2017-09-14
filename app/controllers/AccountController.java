package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Account;
import models.ApplicationStore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import java.util.Set;


public class AccountController extends Controller {

    public Result listAccounts(){
        Set<Account> accounts = ApplicationStore.getInstance().listAccounts();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonData = mapper.convertValue(accounts, JsonNode.class);
        return ok(jsonData);
    }

    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest("JSON data required");

        Account account = Json.fromJson(json, Account.class);
        if (account == null)
            return badRequest("Invalid JSON data");

        account = ApplicationStore.getInstance().createAccount(account);
        return created(Json.toJson(account));
    }

    public Result get(String id) {
        Account account = ApplicationStore.getInstance().getAccount(id);
        if (account == null)
            return notFound("Account with id " + id + " not found");
        return ok(Json.toJson(account));
    }

    public Result update() {
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest("JSON data required");

        Account account = Json.fromJson(json, Account.class);
        if (account == null || account.getId() == null)
            return badRequest("Invalid JSON");

        Account updated = ApplicationStore.getInstance().updateAccount(account);
        if (updated == null)
            return notFound("Account with id " +  account.getId() + " not found");

        return ok(Json.toJson(updated));
    }

    public Result delete(String id) {
        if (!ApplicationStore.getInstance().deleteAccount(id))
            return notFound("Account with id " + id + " not found");
        return noContent();
    }

    public Result deposit(String id, float amount){
        if (!ApplicationStore.getInstance().deposit(id, amount))
            return notFound("Account with id " + id + " not found");
        Account account = ApplicationStore.getInstance().getAccount(id);
        return ok(Json.toJson(account));
    }

    public Result withdraw(String id, float amount){
        try {
            if (!ApplicationStore.getInstance().withdraw(id, amount))
                return notFound("Account with id " + id + " not found");
        }
        catch (Account.InsufficientFundsException ex){
            return badRequest("Account with id " + id + " has insufficient funds");
        }
        Account account = ApplicationStore.getInstance().getAccount(id);
        return ok(Json.toJson(account));
    }

    public Result options() {
        return ok().withHeader("Allow", "GET,POST,PUT,DELETE,OPTIONS");
    }
}