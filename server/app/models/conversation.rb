class Conversation < ActiveRecord::Base
	has_many :conversation_managers
	has_many :users, :through => :conversation_managers
	has_many :messages
end
