class AddLinkedin < ActiveRecord::Migration
	def change
		remove_column :users, :linkedin_api_key, :string
		add_column :users, :linkedin_key, :string
	end
end
