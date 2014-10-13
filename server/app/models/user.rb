class User < ActiveRecord::Base

	has_many :wishes
	has_many :events, :through => :wishes
	has_many :wishlist, :through=> :wishes, :class_name => 'Event', :foreign_key => 'event_id', :source => :event

	has_many :shortlists
	has_many :follows, :through => :shortlists
	has_many :notes
	has_many :conversation_managers
	has_many :conversations, :through => :conversation_managers
	has_many :messages
	has_many :contacts

	validates :email, uniqueness: true, presence: true
	before_create :generate_api_key

	def generate_api_key
		while true do
			self.api_key = SecureRandom.hex
			break unless self.class.exists?(api_key: api_key)
		end
	end
end
