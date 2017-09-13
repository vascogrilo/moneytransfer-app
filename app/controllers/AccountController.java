package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Account;
import models.AccountStore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.Util;

import java.util.Set;


public class AccountController extends Controller {

    public Result listAccounts(){
        Set<Account> accounts = AccountStore.getInstance().getAllAccounts();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonData = mapper.convertValue(accounts, JsonNode.class);
        return ok(Util.createResponse(jsonData, true));
    }

    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest(Util.createResponse("JSON data required", false));

        Account account = AccountStore.getInstance().createAccount(Json.fromJson(json, Account.class));
        JsonNode jsonObject = Json.toJson(account);
        return created(Util.createResponse(jsonObject, true));
    }

    public Result get(String id) {
        Account account = AccountStore.getInstance().getAccount(id);
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

        Account updated = AccountStore.getInstance().updateAccount(account);
        if (updated == null)
            return notFound(Util.createResponse("Account with id " +  account.getId() + " not found", false));

        return ok(Util.createResponse(Json.toJson(updated), true));
    }

    public Result delete(String id) {
        if (!AccountStore.getInstance().deleteAccount(id))
            return notFound(Util.createResponse("Account with id " + id + " not found", false));
        return noContent();
    }

    public Result deposit(String id, float amount){
        if (!AccountStore.getInstance().deposit(id, amount))
            return notFound(Util.createResponse("Account with id " + id + " not found", false));
        return ok(Util.createResponse(Json.toJson(AccountStore.getInstance().getAccount(id)), true));
    }

    public Result withdraw(String id, float amount){
        try {
            if (!AccountStore.getInstance().withdraw(id, amount))
                return notFound(Util.createResponse("Account with id " + id + " not found", false));
        }
        catch (IllegalStateException ex){
            return badRequest(Util.createResponse("Account with id " + id + " has insufficient funds", false));
        }
        return ok(Util.createResponse(Json.toJson(AccountStore.getInstance().getAccount(id)), true));
    }
}