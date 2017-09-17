package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.ApplicationStore;
import models.Transfer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Controller for interacting with the Transfer resource.
 * Provides the following list of operations:
 *  - list all transfers                (GET /transfers)
 *  - create a new transfer             (POST /transfers)
 *  - retrieve a transfer               (GET /transfers/:id)
 *  - delete a transfer                 (DELETE /transfers/:id)
 *  - list of options available         (OPTIONS /transfers)
 */
public class TransferController extends Controller {

    /**
     * Lists all {@link Transfer}s from the store.
     * Allows searching for specific field's values and sorting by a certain field.
     * Refer to {@link ApplicationStore}'s listTransfers for more information.
     * @param originAccountId Optional origin account id to search for.
     * @param destinationAccountId Optional destination account id to search for.
     * @param amount Optional amount to search for.
     * @param aboveAmount Optional amount to search transfers bigger than that (non inclusive).
     * @param belowAmount Optional amount to search transfers smaller than that (non inclusive).
     * @param sort Optional argument for sorting. Should be field name and prepended with '-' for descending order.
     * @return OK with a list of transfers according to input terms.
     */
    public Result listTransfers(String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort) {
        Transfer[] transfers = ApplicationStore.getInstance().listTransfers(originAccountId, destinationAccountId, amount, aboveAmount, belowAmount, sort);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.convertValue(transfers, JsonNode.class);
        return ok(data);
    }

    /**
     * Retrieves an {@link Transfer} by its Id.
     * @param id The transfer's id.
     * @return NOT FOUND if the transfer does not exist or OK with the transfer.
     */
    public Result get(String id){
        Transfer transfer = ApplicationStore.getInstance().getTransfer(id);
        if (transfer == null)
            return notFound("Transfer with id " + id + " not found");
        return ok(Json.toJson(transfer));
    }

    /**
     * Creates a new instance of a {@link Transfer} in {@link ApplicationStore}.
     * Validates request's body as a valid JSON payload for a transfer and if amount is above 0.
     * Asks {@link ApplicationStore} to create and process a new transfer.
     * @return BAD REQUEST if payload is invalid, FORBIDDEN if account ids do not exist or origin account has insufficient funds. CREATED with the new transfer and a Location header for its new path.
     */
    public Result create(){
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest("JSON data required");

        Transfer transfer = Json.fromJson(json, Transfer.class);
        if (transfer == null || transfer.getOriginAccountId() == null || transfer.getDestinationAccountId() == null)
            return badRequest("Invalid JSON data");

        Float amount = transfer.getAmount();
        if (amount <= 0 || amount.isNaN() || amount.isInfinite())
            return badRequest("Transfer amount must be above 0");

        try {
            transfer = ApplicationStore.getInstance().createTransfer(transfer);
            return ok(Json.toJson(transfer)).withHeader("Location", "/transfers/" + transfer.getId());
        } catch (Exception e){
            return forbidden(e.getMessage());
        }
    }

    /**
     * Deletes a specific {@link Transfer}.
     * @param id The transfer's id.
     * @return NOT FOUND if the id does not exist. NO CONTENT if successful.
     */
    public Result delete(String id){
        if (!ApplicationStore.getInstance().deleteTransfer(id))
            return notFound("Transfer with id " + id + " not found");
        return noContent();
    }

    /**
     * Returns a list of options on the {@link Transfer} resource.
     * @return A list of HTTP verbs allowed on the {@link Transfer} resource.
     */
    public Result options(){
        return ok().withHeader("Allow", "GET,POST,DELETE,OPTIONS");
    }
}
