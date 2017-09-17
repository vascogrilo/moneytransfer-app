package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Account;
import models.ApplicationStore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Controller for interacting with the Account resource.
 * Provides the following list of operations:
 *  - list all accounts                 (GET /accounts)
 *  - create a new account              (POST /accounts)
 *  - retrieve an account               (GET /accounts/:id)
 *  - update an account                 (PUT /accounts/:id)
 *  - delete an account                 (DELETE /accounts/:id)
 *  - make a deposit on an account      (PUT /accounts/:id/deposit/:amount)
 *  - make a withdrawal on an account   (PUT /accounts/:id/withdraw/:amount)
 *  - list of options available         (OPTIONS /accounts)
 */
public class AccountController extends Controller {

    /**
     * Lists all {@link Account}s from the store.
     * Allows searching for specific field's values and sorting by a certain field.
     * Refer to {@link ApplicationStore}'s listAccounts for more information.
     * @param name Optional account name to search for.
     * @param ownerName Optional owner name to search for.
     * @param balance Optional balance to search for.
     * @param aboveBalance Optional balance to search accounts richer than that (non inclusive).
     * @param belowBalance Optional balance to search accounts poorer than that (non inclusive).
     * @param sort Optional argument for sorting. Should be field name and prepended with '-' for descending order.
     * @return OK with a list of accounts according to input terms.
     */
    public Result listAccounts(String name, String ownerName, Float balance, Float aboveBalance, Float belowBalance, String sort){
        Account[] accounts = ApplicationStore.getInstance().listAccounts(name, ownerName, balance, aboveBalance, belowBalance, sort);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(accounts, JsonNode.class);
        return ok(jsonData);
    }

    /**
     * Creates a new instance of an {@link Account} in {@link ApplicationStore}.
     * Validates request's body as a valid JSON payload for an Account.
     * If the request payload is valid this operation always succeeds because account ids are generated by the store.
     * @return BAD REQUEST or CREATED with the new account and a Location header for its new path.
     */
    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest("JSON data required");

        Account account = Json.fromJson(json, Account.class);
        if (account == null || account.getName() == null || account.getOwnerName() == null)
            return badRequest("Invalid JSON data");

        account = ApplicationStore.getInstance().createAccount(account);
        return created(Json.toJson(account)).withHeader("Location", "/accounts/" + account.getId());
    }

    /**
     * Retrieves an {@link Account} by its Id.
     * @param id The account's id.
     * @return NOT FOUND if the account does not exist or OK with the account.
     */
    public Result get(String id) {
        Account account = ApplicationStore.getInstance().getAccount(id);
        if (account == null)
            return notFound("Account with id " + id + " not found");
        return ok(Json.toJson(account));
    }

    /**
     * Updates an {@link Account}, specified by its id.
     * @param id The id of the account that should be updated.
     * @return BAD REQUEST if the request payload is invalid JSON for an account. FORBIDDEN if the resource id and updated account id do not match. OK with the new updated account.
     */
    public Result update(String id) {
        if (id == null)
            return badRequest("Resource id is necessary");

        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest("JSON data required");

        Account account = Json.fromJson(json, Account.class);
        if (account == null || account.getId() == null || account.getName() == null || account.getOwnerName() == null)
            return badRequest("Invalid JSON data");
        if (!id.equals(account.getId()))
            return forbidden("Resource id does not match account id");

        Account updated = ApplicationStore.getInstance().updateAccount(account);
        if (updated == null)
            return notFound("Account with id " +  account.getId() + " not found");

        return ok(Json.toJson(updated));
    }

    /**
     * Deletes a specific {@link Account}.
     * @param id The account's id.
     * @return NOT FOUND if the id does not exist. NO CONTENT if successful.
     */
    public Result delete(String id) {
        if (!ApplicationStore.getInstance().deleteAccount(id))
            return notFound("Account with id " + id + " not found");
        return noContent();
    }

    /**
     * Deposits a certain amount to an {@link Account}, identified by its id.
     * @param id The account's id.
     * @param amount The amount to be deposited.
     * @return NOT FOUND if the id does not exist. FORBIDDEN if amount is invalid. OK with the updated account.
     */
    public Result deposit(String id, Float amount){
        try {
            Account account = ApplicationStore.getInstance().deposit(id, amount);
            if (account == null)
                return notFound("Account with id " + id + " not found");
            return ok(Json.toJson(account));
        } catch (IllegalArgumentException e){
            return forbidden(e.getMessage());
        }
    }

    /**
     * Withdraws a certain amount from an {@link Account}, identified by its id.
     * @param id The account's id.
     * @param amount The amount to be withdrawn.
     * @return NOT FOUND if the id does not exist. FORBIDDEN if amount is invalid or has insufficient funds. OK with the updated account.
     */
    public Result withdraw(String id, float amount){
        try {
            Account account = ApplicationStore.getInstance().withdraw(id, amount);
            if (account == null)
                return notFound("Account with id " + id + " not found");
            return ok(Json.toJson(account));
        }
        catch (Exception e){
            return forbidden(e.getMessage());
        }
    }

    /**
     * Returns a list of options on the {@link Account} resource.
     * @return A list of HTTP verbs allowed on the {@link Account} resource.
     */
    public Result options() {
        return ok().withHeader("Allow", "GET,POST,PUT,DELETE,OPTIONS");
    }
}