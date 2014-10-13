module Api
	module V1
		class ConversationController < ApplicationController

			# Need to implement a security, key system due to next line
            skip_before_filter :verify_authenticity_token

            # Validate API_KEY
            before_filter :check_api_key	#, except: [:create, :all]

            # Limit response to only json
            respond_to :json


            def check_api_key
                user = User.find_by(api_key: params[:api_key])
                check = (not user.nil? and user[:id] == params[:id].to_i)
                head :unauthorized unless check
            end

            def create
            	conversation = Conversation.create
            	conversation.save
            	members = params[:members].split ','
            	members.each do |member_id|
            		member = User.find_by id: member_id
            		member.conversations << conversation
            		member.save
            	end
            	render json: {status: "success", id: conversation.id}
            end

            def messages
            	conversation = Conversation.find_by id: params[:conversation_id]
                check = false
                conversation.users.each do |member|
                    if member.id == params[:id].to_i
                        check = true
                        break
                    end
                end
                if check
            	   @messages = conversation.messages
                else
                    render json: {status: "failed", error: "User not part of Conversation"}
                end
            end

            def participants
                conversation = Conversation.find_by id: params[:conversation_id]
                @participants = conversation.users
            end

            def common
                common_conversations = []
                user_list = params[:user_list].split ','
                Conversation.all.each do |conversation|
                    check = true
                    users_in_convo = conversation.users
                    user_list.each do |user|
                        deep_check = false
                        users_in_convo.each do |convo_user|
                            if convo_user.id.to_s == user
                                deep_check = true
                            end
                        end

                        unless deep_check
                            check = false
                            break
                        end
                    end
                    common_conversations << conversation.id if check == true
                end
                if common_conversations.count > 0
                    render json: {status: "success", conversations: common_conversations}
                else
                    render json: {status: "failed"}
                end
            end
		end
	end
end