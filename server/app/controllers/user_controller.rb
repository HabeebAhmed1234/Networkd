class UserController < ApplicationController
    
    # Need to implement a security, key system due to next line
    skip_before_filter :verify_authenticity_token

    # Validate API_KEY
    before_filter :check_api_key, except: [:create, :all]

    def check_api_key
        user = User.find_by(api_key: params[:api_key])
        check = user != nil and user[:id] == params[:id]
        head :unauthorized unless check
    end


    def all
        users = User.all
        users.each do |user|
            # user[:id] = ""
            user[:api_key] = ""
        end
        render json: users
    end


    def show
        print params[:id]
        user = User.find_by id: params[:id]
        render json: user
    end


    def create
        user = User.create(first_name: params[:first_name],
                            last_name: params[:last_name],
                            linkedin_id: params[:linkedin],
                            email: params[:email],
                            gps_coord: params[:gps])
        if user.id == nil
            render :json => {status: "fail", error: "email already exists or invalid"}
        else
            render :json => {status: "success", user: user}
        end
    end


    def update_gps
        user = User.find_by id: params[:id]
        user.update_attribute(:gps_coord, params[:coords])
        render json: user
    end


    def attend_event
        user = User.find_by id: params[:id]
        event  = Event.find_by id: params[:event_id]
        if event != nil
            user.update_attribute(:event_id, params[:event_id])
            render json: {status: "success", user: user}
        else
            render json: {status: "fail"}
        end
    end


    def add_event
        user = User.find_by id: params[:id]
        event = Event.find_by id: params[:event_id]
        if event != nil
            user.wishlist << event
            user.save
            render json: {status: "success", user: user, user_wishlist: user.wishlist}
        else
            render json: {status: "fail"}
        end
    end


    def update
        user = User.find_by id: params[:id]
        p = {}
        p[:first_name] = params[:first_name] unless params[:first_name] == nil
        p[:last_name] = params[:last_name] unless params[:last_name] == nil
        p[:linkedin] = params[:linkedin] unless params[:linkedin] == nil
        p[:email] = params[:email] unless params[:email] == nil
        result = user.update_attributes(p)
        if result
            render json: {status: "success", user: user}
        else
            render json: {status: "fail", error: "email already exists or invalid"}
        end
    end


    def wishlist
        user = User.find_by id: params[:id]
        render json: user.events
    end


    def delete
        user = User.find_by id: params[:id]
        user.destroy
        render json: {status: "success"}
    end
    
end
