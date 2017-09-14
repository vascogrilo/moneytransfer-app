package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Account;
import models.ApplicationStore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.Util;

import java.util.Set;


public class AccountController extends Controller {

    public Result listAccounts(){
        Set<Account> accounts = ApplicationStore.getInstance().listAccounts();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonData = mapper.convertValue(accounts, JsonNode.class);
        return ok(Util.createResponse(jsonData, true));
    }

    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest(Util.createResponse("JSON data required", false));

        Account account = Json.fromJson(json, Account.class);
        if (account == null)
            return badRequest(Util.createResponse("Invalid JSON data", false));

        account = ApplicationStore.getInstance().createAccount(account);
        JsonNode jsonObject = Json.toJson(account);
        return created(Util.createResponse(jsonObject, true));
    }

    public Result get(String id) {
        Account account = ApplicationStore.getInstance().getAccount(id);
        if (account == null)
            return notFound(Util.createResponse("Account with id " + id + " not found", false));
        return ok(Util.createResponse(Json.toJson(account), true));
    }

    public Result update() {
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest(Util.createResponse("JSON data required", false));

        Account account = Json.fromJson(json, Account.class);
        if (account == null || account.getId() == null)
            return badRequest(Util.createResponse("Invalid JSON", false));

        Account updated = ApplicationStore.getInstance().updateAccount(account);
        if (updated == null)
            return notFound(Util.createResponse("Account with id " +  account.getId() + " not found", false));

        return ok(Util.createResponse(Json.toJson(updated), true));
    }

    public Result delete(String id) {
        if (!ApplicationStore.getInstance().deleteAccount(id))
            return notFound(Util.createResponse("Account with id " + id + " not found", false));
        return noContent();
    }

    public Result deposit(String id, float amount){
        if (!ApplicationStore.getInstance().deposit(id, amount))
            return notFound(Util.createResponse("Account with id " + id + " not found", false));
        return ok(Util.createResponse(Json.toJson(ApplicationStore.getInstance().getAccount(id)), true));
    }

    public Result withdraw(String id, float amount){
        try {
            if (!ApplicationStore.getInstance().withdraw(id, amount))
                return notFound(Util.createResponse("Account with id " + id + " not found", false));
        }
        catch (Account.InsufficientFundsException ex){
            return badRequest(Util.createResponse("Account with id " + id + " has insufficient funds", false));
        }
        return ok(Util.createResponse(Json.toJson(ApplicationStore.getInstance().getAccount(id)), true));
    }

    public Result options() {
        return ok().withHeader("Allow", "GET,POST,PUT,DELETE,OPTIONS");
    }
}