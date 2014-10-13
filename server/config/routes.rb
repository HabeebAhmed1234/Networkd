Network::Application.routes.draw do
  
  # Old Routes -- Donot use them, they are only there for backwards compatibility
  # User routes
  match "users", to: "user#all", via: :get
  match "user/:id/:api_key", to: "user#show", via: :get
  match "user/create", to: "user#create", via: :post            # /:first_name/:last_name/:linkedin/:email/:gps
  match "user/update_gps", to: "user#update_gps", via: :put     # /:id/:api_key/:coords
  match "user/attend_event", to: "user#attend_event", via: :put # /:id/:api_key/:event_id
  match "user/add_event", to: "user#add_event", via: :put       # /:id/:api_key/:event_id
  match "user/update", to: "user#update", via: :put             # /:id/:api_key/:first_name/:last_name/:linkedin/:email
  match "user/wishlist/:id/:api_key", to: "user#wishlist", via: :get  
  match "user/delete/:id/:api_key", to: "user#delete", via: :delete   # /:id/:api_key

  # Event routes
  match "events", to: "event#all", via: :get
  match "event/:id", to: "event#show", via: :get
  match "event/create", to: "event#create", via: :post          # ?admin=__&api_key=__&title=__&start_date=__&end_date=__&start_time=__&end_time=__&address=__&gps=__
  match "event/update", to: "event#update", via: :put           # ?id=__&admin=__&api_key=__&title=__&start_date=__&end_date=__&start_time=__&end_time=__&address=__&gps=__
  match "event/delete/:id/:admin/:api_key", to: "event#delete", via: :delete  # /:id/:admin/:api_key

  # API Routing (new routes, please use them)
  namespace :api do
    namespace :v1 do
      # User routes
      match "users", to: "user#all", via: :get
      match "user/:id/:api_key", to: "user#show", via: :get         # /:id/:api_key
      match "user/create", to: "user#create", via: :post            # ?first_name=__&last_name=__&linkedin=__&email=__&gps=__
      match "user/update_gps", to: "user#update_gps", via: :put     # ?id=__api_key=__&coords=__
      match "user/attend_event", to: "user#attend_event", via: :put # ?id=__&api_key=__&event_id=__
      match "user/update", to: "user#update", via: :put             # ?id=__&api_key=__&first_name=__&last_name=__&linkedin=__&email=__
      match "user/delete/:id/:api_key", to: "user#delete", via: :delete   # /:id/:api_keys
      match "user/conversations", to: "user#conversations", via: :get     # ?id=___&api_key=___

      # Event routes
      match "events", to: "event#all", via: :get
      match "event/:id", to: "event#show", via: :get                # /:id
      match "event/create", to: "event#create", via: :post          # ?admin=___&api_key=___&title=___&start_date=___&end_date=___&start_time=___&end_time&=___&address=___&gps=___
      match "event/update", to: "event#update", via: :put           # ?id=___&admin=___&api_key=___&title=___&start_date=___&end_date=___&start_time=___&end_time=___&address=___&gps=___
      match "event/delete/:id/:admin/:api_key", to: "event#delete", via: :delete  # /:id/:admin/:api_key
      
      # Wishlist Routes
      match "wishlist/:id", to: "wishlist#show", via: :get          # /:id
      match "wishlist/add", to: "wishlist#add", via: :put           # ?id=my_id&api_key=my_api_key
      match "wishlist/remove", to: "wishlist#remove", via: :delete  # ?id=my_id&api_key=my_api_key

      # Shortlist Routes
      match "shortlist/:id", to: "shortlist#show", via: :get          # /:id
      match "shortlist/add", to: "shortlist#add", via: :put           # ?id=my_id&api_key=my_api_key&linkedin_id=____
      match "shortlist/remove", to: "shortlist#remove", via: :delete  # ?id=my_id&api_key=my_api_key&linkedin_id=____

      # Note Routes
      match "notes/:api_key/:id", to: "note#show", via: :get          # /:api_key/:id
      match "note/add", to: "note#add", via: :post                     # ?api_key=my_api_key&id=my_id&linkedin_id=sample_linkedin_id&note=my_notes
      match "note/remove", to: "note#remove", via: :delete            # ?api_key=___&id=___&note_id=___
    
      # Conversation Routes
      match "conversation/create", to: "conversation#create", via: :post  # ?id=___&api_key=___&members=___
      match "conversation/messages", to: "conversation#messages", via: :get   # ?id=___&api_key=___&conversation_id=___
      match "conversation/participants", to: "conversation#participants", via: :get   # ?id=___&api_key=___&conversation_id=___
      match "conversation/common", to: "conversation#common", via: :get   # ?id=___&api_key=___&user_list=_comma-delimeted-list_

      # Message Routes
      match "message/create", to: "message#create", via: :post    # ?id=___&api_key=___&conversation_id=___&message=___
    
      # Contact Routes
      match "contact/send", to: "contact#send_contact", via: :post    # ?=id=___&api_key=___&receiver=___&summary=___&skills=___&notes=___
      match "contact/get", to: "contact#view", via: :get    # ?id=___&api_key=___&contact_id=___
      match "contact/delete", to: "contact#delete", via: :delete    # ?id=___&api_key=___&contact_id=___
      match "contact/all", to: "contact#all", via: :get   #?id=___&api_key=___
    end
  end

end
