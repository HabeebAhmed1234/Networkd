class AddWishlist < ActiveRecord::Migration
	def change
		create_table :wishlist do |t|
	  		t.belongs_to :user
	  		t.belongs_to :event
			t.timestamps
		end
	end
end
