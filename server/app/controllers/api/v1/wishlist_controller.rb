module Api
    module V1
        class WishlistController < ApplicationController
        	
        	# Need to implement a security, key system due to next line
            skip_before_filter :verify_authenticity_token

            # Validate API_KEY
            before_filter :check_api_key, except: [:show]

            # Limit response to only json
            respond_to :json


            def check_api_key
                user = User.find_by(api_key: params[:api_key])
                check = (not user.nil? and user[:id] == params[:id].to_i)
                head :unauthorized unless check
            end

        	def show
        		user = User.find_by id: params[:id]
        		@wishlist = user.wishlist
        	end

        	def add
        		user = User.find_by id: params[:id]
                event = Event.find_by id: params[:event_id]
                if event != nil
                    user.wishlist << event
                    user.save
                    render json: {status: "success", user: user}
                else
                    render json: {status: "fail", message: "Event doesnot exist"}
                end
        	end

        	def remove
        		user = User.find_by id: params[:id]
                event = Event.find_by id: params[:event_id]
                if event != nil
                    user.wishlist.destroy(event)
                    user.save
                    render json: {status: "success", user: user}
                else
                    render json: {status: "fail", message: "Event doesnot exist"}
                end
        	end
        end
    end
end
