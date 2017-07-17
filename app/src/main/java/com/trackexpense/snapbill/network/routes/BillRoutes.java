package com.trackexpense.snapbill.network.routes;

import com.trackexpense.snapbill.model.Bill;
import com.trackexpense.snapbill.network.response.Response;
import com.trackexpense.snapbill.network.response.BillsResponse;

import java.util.Date;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Johev on 02-05-2017.
 */

public interface BillRoutes {

    @POST("bill/add/{id}")
    Observable<Response> add(@Path("id") String id, @Body Bill bill);

    @POST("bill/delete/{id}/{billid}")
    Observable<Response> delete(@Path("id") String id, @Path("billid") String billid);

    @POST("bill/update/{id}/{billid}")
    Observable<Response> edit(@Path("id") String id, @Path("billid") String billid, @Body Bill bill);

    @GET("bill/getall/{id}")
    Observable<BillsResponse> getAll(@Path("id") String id);

    @GET("bill/get/{id}/after/{date}")
    Observable<BillsResponse> getAfter(@Path("id") String id, @Path("date") Date date);
}