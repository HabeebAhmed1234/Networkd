module Api
	module V1
		class MessageController < ApplicationController

			# Need to implement a security, key system due to next line
            skip_before_filter :verify_authenticity_token

            # Validate API_KEY
            before_filter :check_api_key	#, except: [:create, :all]

            # Limit response to only json
            respond_to :json


            def check_api_key
                user = User.find_by(api_key: params[:api_key])
                puts user.id
                puts params[:id]
                check = (not user.nil? and user[:id] == params[:id].to_i)
                head :unauthorized unless check
            end

            def create
                conversation = Conversation.find_by id: params[:conversation_id]
                check = false
                conversation.users.each do |member|
                    if member.id == params[:id].to_i
                        check = true
                        break
                    end
                end

                if check
                    message = Message.create(user_id: params[:id], conversation_id: params[:conversation_id], message: params[:message])
                    message.save
                    render json: {status: "success", message: message}
                else    
                    render json: {status: "failed", error: "User not part of Conversation"}
                end
            end

        end
    end
end