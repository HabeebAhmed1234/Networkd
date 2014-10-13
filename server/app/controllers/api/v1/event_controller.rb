module Api
	module V1
		class EventController < ApplicationController

			# Need to implement a security, key system due to next line
			skip_before_filter :verify_authenticity_token

			# Validate API_KEY
		    before_filter :check_api_key, except: [:all, :show]

		    def check_api_key
		        user = User.find_by(api_key: params[:api_key])
		        event = Event.find_by(id: params[:id])
		        check = (user != nil and user[:id] == params[:admin] and event != nil and event[:admin] != params[:admin])
		        head :unauthorized unless check
		    end


			def all
				events = Event.all
				render json: events
			end


			def show
				event = Event.find_by id: params[:id]
				render json: event
			end


			def create
				args = {
					title: params[:title],
					admin: params[:admin],
					start_date: Date.strptime(params[:start_date], '%d-%m-%Y'),
					end_date: Date.strptime(params[:end_date], '%d-%m-%Y'),
					start_time: params[:start_time],
					end_time: params[:end_time],
					address: params[:address],
					gps_coord: params[:gps]
				}

				event = Event.create(args)
				if event.id == nil
		            render :json => {status: "fail", error: "event by title already exists"}
		        else
		            render :json => {status: "success", event: event}
		        end
			end


			def update
				event = Event.find_by id: params[:id]
				args = {}
				args[:title] = params[:title] unless params[:title] == nil
				args[:admin] = params[:admin] unless params[:admin] == nil
				args[:start_date] = Date.strptime(params[:start_date], '%d-%m-%Y') unless params[:start_date] == nil
				args[:end_date] = Date.strptime(params[:end_date], '%d-%m-%Y') unless params[:end_date] == nil
				args[:start_time] = params[:start_time] unless params[:start_time] == nil
				args[:end_time] = params[:end_time] unless params[:end_time] == nil
				args[:address] = params[:address] unless params[:address] == nil
				args[:gps_coord] = params[:gps] unless params[:gps] == nil
			

				event.update_attributes(args)
				render json: event
			end

			def delete
				event = Event.find_by(id: params[:id])
				if event != nil
					event.destroy
					render json: {status: "success"}
				else
					render json: {status: "fail", error: "event does not exits"}
				end
			end

		end
	end
end
