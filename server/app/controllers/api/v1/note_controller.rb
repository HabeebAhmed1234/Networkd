module Api
    module V1
        class NoteController < ApplicationController
        	
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

        	def show
                user = User.find_by id: params[:id] 
                @notes = user.notes
        	end

        	def add
        		user = User.find_by id: params[:id]
                user.notes.create(linkedin_id: params[:linkedin_id], note: params[:note])
                user.save
                render json: {status: "success", user: user, notes: user.notes}
        	end

        	def remove
        		user = User.find_by id: params[:id]
                note = Note.find_by id: params[:note_id]
                if note.nil?
                    render json: {status: "error", message: "Note by ID doesnt exist"}
                else
                    user.notes.destroy(note)
                    render json: {status: "success", user: user, notes: user.notes}
                end
            end
        end
    end
end
