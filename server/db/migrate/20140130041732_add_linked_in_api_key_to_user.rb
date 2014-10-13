class AddLinkedInApiKeyToUser < ActiveRecord::Migration
	def change
		add_column :users, :linkedin_api_key, :string
	end
end
