/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.amp.tms.db.entity.ReportSql;
import com.amp.tms.db.repository.ReportRepository;
import com.amp.tms.exception.ReportNotFoundException;
import com.amp.tms.restfull.pojo.ReportKey;
import java.util.List;
import java.util.Map;
import javax.persistence.QueryTimeoutException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Path("/report")
@Produces(MediaType.APPLICATION_JSON)
public class ReportRest {

    @Autowired
    private ReportRepository repository;

    @GET
    @Path("/sql")
    public List<ReportKey> getQueries() {
        return repository.getReports();
    }

    @POST
    @Path("/sql/{name}")
    @Consumes(MediaType.WILDCARD)
    public Long createQuery(@PathParam("name") String name, String sql) {
        validateSql(sql);
        return repository.createReport(name, sql);
    }

    @Path("/sql/{sqlPk}")
    @PUT
    @Consumes(MediaType.WILDCARD)
    public void updateQuery(@PathParam("sqlPk") Long pk, String sql) {
        validateSql(sql);
        try {
            repository.updateReport(pk, sql);
        } catch (ReportNotFoundException ex) {
            throw new NotFoundException(ex);
        }
    }

    @Path("/sql/{sqlPk}")
    @DELETE
//    @Consumes(MediaType.WILDCARD)
    public void deleteQuery(@PathParam("sqlPk") Long pk) {
        try {
            repository.deleteReport(pk);
        } catch (ReportNotFoundException ex) {
            throw new NotFoundException(ex);
        }
    }

    @Path("/sql/{sqlPk}")
    @GET
    public ReportSql getQuery(@PathParam("sqlPk") Long pk) {
        try {
            return repository.getReport(pk);
        } catch (ReportNotFoundException ex) {
            throw new NotFoundException(ex);
        }
    }

    @Path("/sql/{sqlPk}/run")
    @GET
//    @Consumes(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> runQuery(@PathParam("sqlPk") Long pk) {
        try {
            return repository.runReport(pk);
        } catch (ReportNotFoundException ex) {
            throw new NotFoundException(ex);
        } catch(QueryTimeoutException ex){
            throw new ServerErrorException(504, ex);
        }
    }

    private void validateSql(String sql) {
        if (sql.matches("(?i)(\\s|[()])(alter|create|delete|drop|execute|insert|merge|replace|truncate|update)(\\s|[()])")) {
            throw new BadRequestException();
        }
    }
}
