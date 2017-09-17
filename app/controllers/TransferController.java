package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.ApplicationStore;
import models.Transfer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class TransferController extends Controller {

    public Result listTransfers(String id, String originAccountId, String destinationAccountId, Float amount, Float aboveAmount, Float belowAmount, String sort) {
        Transfer[] transfers = ApplicationStore.getInstance().listTransfers(id, originAccountId, destinationAccountId, amount, aboveAmount, belowAmount, sort);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.convertValue(transfers, JsonNode.class);
        return ok(data);
    }

    public Result get(String id){
        Transfer transfer = ApplicationStore.getInstance().getTransfer(id);
        if (transfer == null)
            return notFound("Transfer with id " + id + " not found");
        return ok(Json.toJson(transfer));
    }

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
            return ok(Json.toJson(transfer));
        } catch (Exception e){
            return forbidden(e.getMessage());
        }
    }

    public Result delete(String id){
        if (!ApplicationStore.getInstance().deleteTransfer(id))
            return notFound("Transfer with id " + id + " not found");
        return noContent();
    }

    public Result options(){
        return ok().withHeader("Allow", "GET,POST,DELETE,OPTIONS");
    }
}
