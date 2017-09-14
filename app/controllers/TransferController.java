package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.ApplicationStore;
import models.Transfer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.Util;
import java.util.Set;

public class TransferController extends Controller {

    public Result listTransfers() {
        Set<Transfer> transfers = ApplicationStore.getInstance().listTransfers();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.convertValue(transfers, JsonNode.class);
        return ok(Util.createResponse(data, true));
    }

    public Result get(String id){
        Transfer transfer = ApplicationStore.getInstance().getTransfer(id);
        if (transfer == null)
            return notFound(Util.createResponse("Transfer with id " + id + " not found", false));
        return ok(Util.createResponse(Json.toJson(transfer), true));
    }

    public Result create(){
        JsonNode json = request().body().asJson();
        if (json == null)
            return badRequest(Util.createResponse("JSON data required", false));

        Transfer transfer = Json.fromJson(json, Transfer.class);
        if (transfer == null)
            return badRequest(Util.createResponse("Invalid JSON data", true));

        try {
            transfer = ApplicationStore.getInstance().createTransfer(transfer);
            return ok(Util.createResponse(Json.toJson(transfer), true));
        } catch (Exception e){
            return forbidden(Util.createResponse(e.getMessage(), false));
        }
    }

    public Result delete(String id){
        if (!ApplicationStore.getInstance().deleteTransfer(id))
            return notFound(Util.createResponse("Transfer with id " + id + " not found", false));
        return noContent();
    }

    public Result options(){
        return ok().withHeader("Allow", "GET,POST,DELETE,OPTIONS");
    }
}
