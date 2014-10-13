class Contact < ActiveRecord::Base
	belongs_to :user
	belongs_to :sender, :class_name => 'User', :foreign_key => 'sender_id'
end
