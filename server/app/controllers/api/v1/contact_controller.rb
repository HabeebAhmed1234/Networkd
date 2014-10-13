module Api
    module V1
        class 	ContactController < ApplicationController
        	
        	# Need to implement a security, key system due to next line
            skip_before_filter :verify_authenticity_token

            # Validate API_KEY
            before_filter :check_api_key

            # Limit response to only json
            respond_to :json

            def check_api_key
                user = User.find_by(api_key: params[:api_key])
                check = (not user.nil? and user[:id] == params[:id].to_i)
                head :unauthorized unless check
            end

        	def send_contact
        		receiver = User.find_by(id: params[:receiver])
        		user = User.find_by(id: params[:id])
        		if not receiver.nil?
        			contact = Contact.create(
        				summary: params[:summary],
        				skills: params[:skills],
        				extra_notes: params[:notes]
        			)

        			contact.sender = user
        			contact.save
        			receiver.contacts << contact
        			receiver.save
        			
        			render json: {status: "success", contact: contact}
        		else
        			render json: {status: "fail", message: "User by ID: #{params[:receiver]} does not exist"}
        		end
        	end

            def delete
                contact = Contact.find_by(id: params[:contact_id])
                if not contact.nil?
                    contact.destroy
                    render json: {status: "success", message: "Contact deleted"}
                else
                    render json: {status: "fail", message: "No contact by ID #{params[:contact_id]} exists"}
                end
            end

            def view
                contact = Contact.find_by(id: params[:contact_id])
                if not contact.nil?
                    render json: {status: "success", contact: contact}
                else
                    render json: {status: "fail", message: "No contact by ID #{params[:contact_id]} exists"}
                end
            end

            def all
                user = User.find_by(id: params[:id])
                @contacts = user.contacts
            end

        end
    end
end