/**
 * 
 */
package com.learnit.aws.rekognition;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectProtectiveEquipmentRequest;
import com.amazonaws.services.rekognition.model.DetectProtectiveEquipmentResult;
import com.amazonaws.services.rekognition.model.EquipmentDetection;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.ProtectiveEquipmentBodyPart;
import com.amazonaws.services.rekognition.model.ProtectiveEquipmentPerson;
import com.amazonaws.services.rekognition.model.ProtectiveEquipmentSummarizationAttributes;
import com.amazonaws.services.rekognition.model.ProtectiveEquipmentSummary;
import com.amazonaws.services.rekognition.model.ProtectiveEquipmentType;
import com.amazonaws.services.rekognition.model.S3Object;

/**
 * @author learnIT.
 *
 */
public class MaskDetectionDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String imageToBeAnalysed = "input1.JPG";
		String bucketName = "learn-it-demo-bucket";

		// Create AWS Rekognition Client object.
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
				.withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(Regions.US_EAST_1).build();

		// create list of required Protective Equipment Types like face cover, head
		// cover, hand covers.
		List<String> requiredEquipmentTypes = new ArrayList<String>();

		// set face cover as required type for the persons in the image.
		requiredEquipmentTypes.add(ProtectiveEquipmentType.FACE_COVER.name());

		// creating a summarizationAttributes object using requiredEquipmentTypes.
		ProtectiveEquipmentSummarizationAttributes summarizationAttributes = new ProtectiveEquipmentSummarizationAttributes();
		summarizationAttributes.setRequiredEquipmentTypes(requiredEquipmentTypes);
		summarizationAttributes.setMinConfidence(75F);

		// Creating a final request for face covers detection.
		DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
				.withSummarizationAttributes(summarizationAttributes)
				.withImage(new Image().withS3Object(new S3Object().withName(imageToBeAnalysed).withBucket(bucketName)));

		try {
			DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);
			List<ProtectiveEquipmentPerson> identifiedPersons = result.getPersons();

			System.out.println("Detected Persons For :" + imageToBeAnalysed);
			for (ProtectiveEquipmentPerson person : identifiedPersons) {
				System.out.println("Person ID :" + person.getId());
				List<ProtectiveEquipmentBodyPart> bodyParts = person.getBodyParts();
				for (ProtectiveEquipmentBodyPart bodyPart : bodyParts) {
					System.out.println("BodyPart Name: " + bodyPart.getName());
					System.out.println("BodyPart Confidence: " + bodyPart.getConfidence());
					for (EquipmentDetection equipmentDetection : bodyPart.getEquipmentDetections()) {
						System.out.println("EquipmentDetection Type :" + equipmentDetection.getType());
						String isCovered = (equipmentDetection.getCoversBodyPart().getValue()) ? "YES" : "NO";
						System.out.println("Covered : " + isCovered);
					}
				}
				System.out.println("---------------------------------------------------------------------");
			}

			ProtectiveEquipmentSummary protectiveEquipmentSummary = result.getSummary();
			int totalPersons = protectiveEquipmentSummary.getPersonsIndeterminate().size()
					+ protectiveEquipmentSummary.getPersonsWithoutRequiredEquipment().size()
					+ protectiveEquipmentSummary.getPersonsWithRequiredEquipment().size();
			System.out.println("Summary for the input image :");
			System.out.println("Total Persons Found : " + totalPersons);
			System.out.println("Total Persons with Face Cover : "
					+ protectiveEquipmentSummary.getPersonsWithRequiredEquipment().size());
			System.out.println("Total Persons without Face Cover : "
					+ protectiveEquipmentSummary.getPersonsWithoutRequiredEquipment().size());
			System.out.println(
					"Total Indeterminable Persons : " + protectiveEquipmentSummary.getPersonsIndeterminate().size());

		} catch (AmazonRekognitionException e) {
			e.printStackTrace();
		}
	}

}
