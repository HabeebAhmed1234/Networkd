class CreateShortlists < ActiveRecord::Migration
  def change
    create_table :shortlists do |t|
      t.integer :user_id
      t.integer :follow_id

      t.timestamps
    end
  end
end
