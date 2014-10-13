class CreateUsers < ActiveRecord::Migration
  def change
    create_table :users do |t|
      t.string :first_name
      t.string :last_name
      t.string :linkedin_id
      t.string :email
      t.string :gps_coord
      t.references :event, index: true
      t.string :event_wishlist

      t.timestamps
    end
  end
end
