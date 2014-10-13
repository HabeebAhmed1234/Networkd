module Api
    module V1
        class UserController < ApplicationController
            
            # Need to implement a security, key system due to next line
            skip_before_filter :verify_authenticity_token

            # Validate API_KEY
            before_filter :check_api_key, except: [:create, :all]

            # Limit response to only json
            respond_to :json


            def check_api_key
                user = User.find_by(api_key: params[:api_key])
                check = (not user.nil? and user[:id] == params[:id].to_i)
                head :unauthorized unless check
            end


            def all
                @users = User.all
            end


            def show
                print params[:id]
                @user = User.find_by id: params[:id]
            end


            def create
                # Check if user exists
                ex_user = User.find_by linkedin_key: params[:linkedin_key]
                
                # If user exists with linkedin_key
                if not ex_user.nil?
                    render :json => {status: "success", user: ex_user}
                else
                    user = User.create(first_name: params[:first_name],
                                        last_name: params[:last_name],
                                        linkedin_id: params[:linkedin],
                                        email: params[:email],
                                        gps_coord: params[:gps],
                                        linkedin_key: params[:linkedin_key])
                    if user.id == nil
                        render :json => {status: "fail", error: "email already exists or invalid"}
                    else
                        render :json => {status: "success", user: user}
                    end
                end
            end


            def update_gps
                @user = User.find_by id: params[:id]
                @user.update_attribute(:gps_coord, params[:coords])
                @user.save
            end


            def attend_event
                user = User.find_by id: params[:id]
                event  = Event.find_by id: params[:event_id]
                if event != nil
                    user.update_attribute(:attend, params[:event_id])
                    user.save
                    render json: {status: "success", user: user}
                else
                    render json: {status: "fail"}
                end
            end


            def update
                user = User.find_by id: params[:id]
                p = {}
                p[:first_name] = params[:first_name] unless params[:first_name].nil?
                p[:last_name] = params[:last_name] unless params[:last_name].nil?
                p[:linkedin] = params[:linkedin] unless params[:linkedin].nil?
                p[:email] = params[:email] unless params[:email].nil?
                p[:linkedin_key] = params[:linkedin_key] unless params[:linkedin_key].nil?
                result = user.update_attributes(p)
                if result
                    render json: {status: "success", user: user}
                else
                    render json: {status: "fail", error: "email already exists or invalid"}
                end
            end


            def delete
                user = User.find_by id: params[:id]
                user.destroy
                render json: {status: "success"}
            end


            def conversations
                user = User.find_by id: params[:id]
                conversations = user.conversations
                @conversations = []
                conversations.each do |convo|
                    convo_rep = {}
                    convo_rep[:id] = convo.id
                    convo_rep[:participants] = []
                    convo.users.each do |user|
                        user_name = user.last_name + ", " + user.first_name
                        convo_rep[:participants] << user_name
                    end
                    @conversations << convo_rep              
                end
                render json: {status: "success", conversations: @conversations}
            end
            
        end
    end
end
