module Api
    module V1
        class ShortlistController < ApplicationController
        	
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
                @shortlist = user.follows
        	end

        	def add
        		user = User.find_by id: params[:id]
                to_follow = User.find_by linkedin_id: params[:linkedin_id]
                if to_follow != nil
                    # Check if user is already following
                    exists = false
                    user.follows.each do |f|
                        if f.linkedin_id == params[:linkedin_id]
                            exists = true
                            break
                        end
                    end

                    # If user exists in follow list
                    if exists
                        render json: {staus: "fail", message: "User by LinkedIn ID is already being followed"}
                    else
                        user.follows << to_follow
                        user.save
                        render json: {status: "success", user: user}
                    end
                else
                    render json: {status: "fail", message: "User by LinkedIn ID doesnt exist"}
                end
        	end

        	def remove
        		user = User.find_by id: params[:id]
                to_follow = User.find_by linkedin_id: params[:linkedin_id]
                if to_follow != nil
                    user.follows.destroy(to_follow)
                    user.save
                    render json: {status: "success", user: user}
                else
                    render json: {status: "fail", message: "User by LinkedIn ID doesnt exist"}
                end
        	end
        end
    end
end
