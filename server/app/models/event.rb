class Event < ActiveRecord::Base
	has_many :wishes
	has_many :users, :through => :wishes
	validates :title, uniqueness: true
end
