class RenameWishlist < ActiveRecord::Migration
  def change
  	rename_table :wishlist, :wishes
  end
end
