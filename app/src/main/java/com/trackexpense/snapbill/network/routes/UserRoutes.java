package com.trackexpense.snapbill.network.routes;

import com.trackexpense.snapbill.network.response.Response;
import com.trackexpense.snapbill.model.User;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Johev on 02-05-2017.
 */

public interface UserRoutes {

    @POST("user/register")
    Observable<Response> register(@Body User user);

    @GET("user/activate/{email}/{token}")
    Observable<Response> activate(@Path("email") String email,@Path("token") String token);

    @POST("user/authenticate")
    Observable<Response> login();

    @POST("user/is-authorized/{id}")
    Observable<Response> isAuthorized(@Path("id") String id);

    @GET("user/{email}")
    Observable<User> getProfile(@Path("email") String email);

    @PUT("user/pass/change/{email}")
    Observable<Response> changePassword(@Path("email") String email, @Body User user);

    @POST("user/pass/reset/{email}")
    Observable<Response> resetPasswordInit(@Path("email") String email);

    @POST("user/pass/reset/{email}")
    Observable<Response> resetPasswordFinish(@Path("email") String email, @Body User user);
}